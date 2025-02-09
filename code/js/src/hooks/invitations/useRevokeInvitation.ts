import { useState } from "react";
import { revokeInvitations } from "../../api/invitations";

const useRevokeInvitation = (channelId: number) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const revokeInvitation = async (invitationId: number) => {
    setLoading(true);
    setError(null);
    try {
      await revokeInvitations(channelId, invitationId);
    } catch (err: any) {
      setError(err.message || "Failed to revoke invitation.");
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { revokeInvitation, loading, error };
};

export default useRevokeInvitation;
