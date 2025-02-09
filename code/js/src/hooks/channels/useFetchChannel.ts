import { fetchChannelDetails } from "../../api/channels";
import { useEffect, useState } from "react";
import { Channel } from "../../types";

const useFetchChannel = (channelId: number | null) => {
  const [channel, setChannel] = useState<Channel | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!channelId) {
      setError("Invalid channel ID.");
      setLoading(false);
      return;
    }

    const loadChannel = async () => {
      setLoading(true);
      try {
        const data = await fetchChannelDetails(channelId);
        setChannel(data);
      } catch (err: any) {
        setError(err.message || "Failed to fetch channel details.");
      } finally {
        setLoading(false);
      }
    };

    loadChannel();
  }, [channelId]);

  return { channel, loading, error };
};

export default useFetchChannel;
