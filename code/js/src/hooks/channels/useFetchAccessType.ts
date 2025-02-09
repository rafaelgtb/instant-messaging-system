import { useEffect, useState } from "react";
import { fetchMemberAccessType } from "../../api/channels";
import useAuth from "../auth/useAuth";

const useFetchAccessType = (channelId: number | null) => {
  const { user } = useAuth();
  const [accessType, setAccessType] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!channelId || !user?.id) {
      setError("Invalid channel or user ID.");
      setLoading(false);
      return;
    }

    const loadAccessType = async () => {
      setLoading(true);
      try {
        const data = await fetchMemberAccessType(user.id, channelId);
        setAccessType(data);
      } catch (err: any) {
        setError(err.message || "Failed to fetch access type.");
      } finally {
        setLoading(false);
      }
    };

    loadAccessType();
  }, [user?.id, channelId]);

  return { accessType, loading, error };
};

export default useFetchAccessType;
