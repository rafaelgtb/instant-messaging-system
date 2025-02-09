import { useEffect, useState } from "react";
import { fetchUserChannels } from "../../api/users";
import { Channel } from "../../types";
import useAuth from "../auth/useAuth";

interface UseFetchUserChannelsResult {
  channels: Channel[];
  loading: boolean;
  error: string | null;
}

const useFetchUserChannels = (): UseFetchUserChannelsResult => {
  const { user } = useAuth();
  const [channels, setChannels] = useState<Channel[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!user?.id) {
      setError("User is not authenticated");
      setChannels([]);
      setLoading(false);
      return;
    }

    const loadChannels = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await fetchUserChannels();
        setChannels(data);
      } catch (err: any) {
        setError(err.message || "Failed to load channels.");
      } finally {
        setLoading(false);
      }
    };

    loadChannels();
  }, [user]);

  return { channels, loading, error };
};

export default useFetchUserChannels;
