import { Dispatch, FormEvent, SetStateAction, useState } from "react";
import { useNavigate } from "react-router-dom";
import { createChannel } from "../../api/channels";

interface CreateChannelHook {
  channelName: string;
  setChannelName: Dispatch<SetStateAction<string>>;
  isPublic: boolean;
  setIsPublic: Dispatch<SetStateAction<boolean>>;
  handleSubmit: (e: FormEvent<HTMLFormElement>) => void;
  loading: boolean;
  error: string | null;
}

const useCreateChannel = (): CreateChannelHook => {
  const [channelName, setChannelName] = useState<string>("");
  const [isPublic, setIsPublic] = useState<boolean>(true);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);

    const trimmedName = channelName.trim();
    if (trimmedName.length < 3) {
      setError("Channel name must be at least 3 characters long.");
      return;
    }

    setLoading(true);
    try {
      const newChannel = await createChannel({ name: trimmedName, isPublic });
      navigate(`/channels/${newChannel.id}`);
    } catch (err: any) {
      setError(err.message || "Failed to create channel.");
    } finally {
      setLoading(false);
    }
  };

  return {
    channelName,
    setChannelName,
    isPublic,
    setIsPublic,
    handleSubmit,
    loading,
    error,
  };
};

export default useCreateChannel;
