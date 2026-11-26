const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");
const { onDocumentUpdated } = require("firebase-functions/v2/firestore");

initializeApp();

const db = getFirestore();
const statusNames = {
  PENDING: "pendiente",
  ACCEPTED: "aceptada",
  COMPLETED: "completada",
  CANCELLED: "cancelada",
};

exports.notifyClientOnRequestStatusChange = onDocumentUpdated(
  { document: "requests/{requestId}", retry: true },
  async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();
    const recipientUid = after?.clientId;
    const status = after?.status;
    const requestId = event.params.requestId;

    if (!before || !after || !recipientUid || before.status === status || !status) {
      return;
    }

    const tokenSnapshot = await db
      .collection("users")
      .doc(recipientUid)
      .collection("fcmTokens")
      .get();
    const tokenDocuments = tokenSnapshot.docs.filter((document) => document.get("token"));
    if (tokenDocuments.length === 0) return;

    const body = `Tu solicitud ${requestId} ahora está ${statusNames[status] || status}.`;
    const invalidTokenCodes = new Set([
      "messaging/invalid-registration-token",
      "messaging/registration-token-not-registered",
    ]);

    // FCM acepta hasta 500 tokens por envío multicast.
    for (let offset = 0; offset < tokenDocuments.length; offset += 500) {
      const batch = tokenDocuments.slice(offset, offset + 500);
      const response = await getMessaging().sendEachForMulticast({
        tokens: batch.map((document) => document.get("token")),
        data: {
          title: "Actualización de solicitud",
          body,
          requestId,
          status,
        },
        android: { priority: "high" },
      });

      const invalidDocuments = [];
      response.responses.forEach((result, index) => {
        if (!result.success && invalidTokenCodes.has(result.error?.code)) {
          invalidDocuments.push(batch[index].ref);
        }
      });
      if (invalidDocuments.length > 0) {
        const cleanup = db.batch();
        invalidDocuments.forEach((reference) => cleanup.delete(reference));
        await cleanup.commit();
      }
    }
  }
);
