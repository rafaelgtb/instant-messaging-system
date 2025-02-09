import { useState } from "react";
import { leaveChannel } from "../../api/channels";
import { useNavigate } from "react-router-dom";

interface UseLeaveChannelReturn {
  handleLeaveChannel: () => Promise<void>;
  loading: boolean;
  error: string | null;
}

const useLeaveChannel = (channelId: number | null): UseLeaveChannelReturn => {
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const handleLeaveChannel = async () => {
    if (!channelId) {
      setError("Invalid channel ID.");
      setLoading(false);
      return;
    }

    setLoading(true);
    try {
      await leaveChannel(channelId);
      navigate("/channels");
    } catch (err: any) {
      setError(err.message || "Failed to leave the channel.");
    } finally {
      setLoading(false);
    }
  };

  return { handleLeaveChannel, loading, error };
};

export default useLeaveChannel;
