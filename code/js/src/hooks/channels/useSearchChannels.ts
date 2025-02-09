import { useEffect, useRef, useState } from "react";
import { Channel } from "../../types";
import { searchChannels } from "../../api/channels";

const useSearchChannels = () => {
  const [query, setQuery] = useState<string>("");
  const [channels, setChannels] = useState<Channel[]>([]);
  const [loadingSearch, setLoadingSearch] = useState<boolean>(false);
  const [errorSearch, setErrorSearch] = useState<string | null>(null);

  const searchIdRef = useRef<number>(0);

  useEffect(() => {
    const trimmedQuery = query.trim();

    setLoadingSearch(true);
    setErrorSearch(null);

    searchIdRef.current += 1;
    const currentSearchId = searchIdRef.current;

    const delayDebounce = setTimeout(() => {
      const performSearch = async () => {
        try {
          const publicChannels = await searchChannels(trimmedQuery);
          if (currentSearchId === searchIdRef.current) {
            setChannels(publicChannels);
            setErrorSearch(null);
          }
        } catch (err: any) {
          if (currentSearchId === searchIdRef.current) {
            setErrorSearch(err.message || "Failed to load public channels.");
            setChannels([]);
          }
        } finally {
          if (currentSearchId === searchIdRef.current) {
            setLoadingSearch(false);
          }
        }
      };
      performSearch();
    }, 500);

    return () => clearTimeout(delayDebounce);
  }, [query]);

  return { query, setQuery, channels, loadingSearch, errorSearch };
};

export default useSearchChannels;
