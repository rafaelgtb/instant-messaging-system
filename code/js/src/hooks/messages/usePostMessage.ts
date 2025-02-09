import { useCallback, useState } from "react";
import { postNewMessage } from "../../api/messages";

interface UsePostMessageReturn {
  handlePostMessage: (messageContent: string) => Promise<void>;
  loading: boolean;
  error: string | null;
}

const usePostMessage = (channelId: number | null): UsePostMessageReturn => {
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const handlePostMessage = useCallback(
    async (messageContent: string) => {
      if (!channelId) {
        setError("Invalid channel ID.");
        return;
      }

      const trimmedMessage = messageContent.trim();
      if (!trimmedMessage) {
        setError("Message cannot be empty.");
        return;
      }

      setLoading(true);
      try {
        await postNewMessage(channelId, trimmedMessage);
      } catch (error: any) {
        setError(error.message || "Failed to post the message.");
      } finally {
        setLoading(false);
      }
    },
    [channelId],
  );

  return { handlePostMessage, loading, error };
};

export default usePostMessage;
