import { useEffect, useState } from "react";
import { Message } from "../../types";
import { fetchMessages } from "../../api/messages";

const useFetchMessages = (channelId: number | null) => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!channelId) {
      setError("Invalid channel ID.");
      setLoading(false);
      return;
    }

    const loadMessages = async () => {
      setLoading(true);
      try {
        const data = await fetchMessages(channelId);
        setMessages(data);
      } catch (err: any) {
        setError(err.message || "Failed to fetch messages.");
      } finally {
        setLoading(false);
      }
    };

    loadMessages();
  }, [channelId]);

  return { messages, setMessages, loading, error };
};

export default useFetchMessages;
