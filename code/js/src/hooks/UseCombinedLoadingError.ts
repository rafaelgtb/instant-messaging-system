interface UseCombinedLoadingError {
  isLoading: boolean;
  error: string | null;
}

const useCombinedLoadingError = (
  ...hooks: { loading: boolean; error: string | null }[]
): UseCombinedLoadingError => {
  const isLoading = hooks.some((hook) => hook.loading);
  const error =
    hooks.map((hook) => hook.error).find((err) => err !== null) || null;
  return { isLoading, error };
};

export default useCombinedLoadingError;
