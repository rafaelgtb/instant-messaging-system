import { useEffect, useState } from "react";
import { Invitation } from "../../types";
import { fetchInvitations } from "../../api/invitations";

const useManageInvitations = (channelId: number) => {
  const [invitations, setInvitations] = useState<Invitation[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const loadInvitations = async () => {
    if (!channelId) {
      setError("Invalid channel ID.");
      setLoading(false);
      return;
    }
    setLoading(true);
    try {
      const data = await fetchInvitations(channelId);
      setInvitations(data);
    } catch (err: any) {
      setError(err.message || "Failed to load invitations.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInvitations();
  }, [channelId]);

  return { invitations, loading, error, reloadInvitations: loadInvitations };
};

export default useManageInvitations;
