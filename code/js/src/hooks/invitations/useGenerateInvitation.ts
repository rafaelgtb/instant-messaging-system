import { useState } from "react";
import { Invitation } from "../../types";
import { createInvitation } from "../../api/invitations";

interface GenerateInvitationInput {
  channelId: number;
  accessType: "READ_ONLY" | "READ_WRITE";
  expiresAt: string;
}

const useGenerateInvitation = () => {
  const [invitation, setInvitation] = useState<Invitation | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const generateInvitation = async (input: GenerateInvitationInput) => {
    setLoading(true);
    try {
      const invitationData = await createInvitation(input);
      setInvitation(invitationData);
    } catch (err: any) {
      setError(err.message || "Failed to create invitation.");
    } finally {
      setLoading(false);
    }
  };

  return { invitation, generateInvitation, loading, error };
};

export default useGenerateInvitation;
