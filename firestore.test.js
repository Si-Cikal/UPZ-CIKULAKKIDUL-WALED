const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const DATABASE_ID = "ai-studio-android-upzcikul-aa675635-ab25-48d3-976d-ff90e72057e5";
const ALICE_UID = "amil_alice_01";
const BOB_UID = "amil_bob_02";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function getDb(context) {
  return context.firestore({ databaseId: DATABASE_ID });
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write any UPZ collection", async () => {
  const unauthDb = getDb(testEnv.unauthenticatedContext());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("zakat_transactions").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("mustahik").get());
});

test("Authenticated amil can create and update own profile, but cannot read Bob's profile", async () => {
  const aliceDb = getDb(testEnv.authenticatedContext(ALICE_UID));
  const bobDb = getDb(testEnv.authenticatedContext(BOB_UID));
  const now = new Date();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      namaPetugas: "Ust. Ahmad Fauzi",
      jabatan: "Ketua UPZ",
      dusun: "Dusun I (Manis)",
      desa: "Desa Cikulak Kidul, Kec. Waled, Kab. Cirebon",
      targetFitrahBerasKg: 2500.0,
      targetZakatUang: 50000000,
      hargaBerasPerKg: 14000,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Ghost field shadow update fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(),
    })
  );

  // Cross-user read fails
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated amil can create zakat transaction, verify it (terminal lock), and Bob is isolated", async () => {
  const aliceDb = getDb(testEnv.authenticatedContext(ALICE_UID));
  const bobDb = getDb(testEnv.authenticatedContext(BOB_UID));
  const now = new Date();

  const txRef = aliceDb.collection("users").doc(ALICE_UID).collection("zakat_transactions").doc("tx_001");
  await assertSucceeds(
    txRef.set({
      userId: ALICE_UID,
      nomorKwitansi: "UPZ-CK-2026-001",
      namaMuzakki: "H. Abdullah",
      rtRw: "RT 002 / RW 001 - Dusun Manis",
      jenisZakat: "fitrah_uang",
      jumlahJiwa: 4,
      nominalUang: 140000,
      beratBerasKg: 0.0,
      metodePembayaran: "tunai",
      catatan: "Zakat fitrah keluarga",
      status: "tercatat",
      createdAt: now,
      updatedAt: now,
    })
  );

  // State shortcutting: cannot create directly as terverifikasi
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("zakat_transactions").doc("tx_002").set({
      userId: ALICE_UID,
      nomorKwitansi: "UPZ-CK-2026-002",
      namaMuzakki: "Ibu Siti",
      rtRw: "RT 001 / RW 001",
      jenisZakat: "mal",
      jumlahJiwa: 0,
      nominalUang: 500000,
      beratBerasKg: 0.0,
      metodePembayaran: "transfer",
      catatan: "",
      status: "terverifikasi",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Cross-user list and get fail
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("zakat_transactions").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("zakat_transactions").doc("tx_001").get());

  // Alice can verify
  await assertSucceeds(
    txRef.update({
      status: "terverifikasi",
      updatedAt: new Date(),
    })
  );

  // Terminal state lock: once terverifikasi, subsequent update fails
  await assertFails(
    txRef.update({
      nominalUang: 999999,
      updatedAt: new Date(),
    })
  );
});

test("Authenticated amil can create mustahik, transition to disalurkan, and lock terminal state", async () => {
  const aliceDb = getDb(testEnv.authenticatedContext(ALICE_UID));
  const bobDb = getDb(testEnv.authenticatedContext(BOB_UID));
  const now = new Date();

  const mRef = aliceDb.collection("users").doc(ALICE_UID).collection("mustahik").doc("m_001");
  await assertSucceeds(
    mRef.set({
      userId: ALICE_UID,
      namaMustahik: "Ibu Aminah",
      golonganAsnaf: "fakir",
      rtRw: "RT 004 / RW 002 - Dusun Pahing",
      statusPenyaluran: "terdaftar",
      bantuanUang: 250000,
      bantuanBerasKg: 5.0,
      catatan: "Lansia sebatang kara",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("mustahik").get());

  await assertSucceeds(
    mRef.update({
      statusPenyaluran: "disalurkan",
      updatedAt: new Date(),
    })
  );

  // Terminal state lock
  await assertFails(
    mRef.update({
      bantuanUang: 500000,
      updatedAt: new Date(),
    })
  );
});
