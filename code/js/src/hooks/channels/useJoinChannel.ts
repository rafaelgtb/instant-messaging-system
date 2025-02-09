import { useState } from "react";
import { joinChannel, joinChannelByToken } from "../../api/channels";

const useJoinChannel = (joinFunction: (arg: any) => Promise<void>) => {
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const handleJoin = async (arg: any) => {
    setLoading(true);
    try {
      await joinFunction(arg);
    } catch (err: any) {
      setError(err.message || "Failed to join the channel.");
    } finally {
      setLoading(false);
    }
  };

  return { handleJoin, loading, error };
};

export const useJoinPublicChannel = (channelId: number) => {
  return useJoinChannel(async () => await joinChannel(channelId));
};

export const useJoinPrivateChannel = () => {
  return useJoinChannel(joinChannelByToken);
};
