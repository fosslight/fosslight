import axios, { AxiosRequestConfig } from 'axios';
import qs from 'qs';
import { useMutation } from 'react-query';

export function useAPI(
  method: 'get' | 'post' | 'put' | 'patch' | 'delete',
  url: string,
  config?: {
    onStart?: () => void;
    onSuccess?: (res: any) => void;
    onError?: (err: unknown) => void;
    onFinish?: () => void;
    type?: 'json' | 'file';
  }
) {
  const mutationOptions: any = {};
  if (config?.onStart) {
    mutationOptions.onMutate = config.onStart;
  }
  if (config?.onSuccess) {
    mutationOptions.onSuccess = config.onSuccess;
  }
  if (config?.onError) {
    mutationOptions.onError = config.onError;
  }
  if (config?.onFinish) {
    mutationOptions.onSettled = config.onFinish;
  }

  const mutation = useMutation(async (data: { params?: any; body?: any } | null) => {
    const requestConfig: AxiosRequestConfig = { method, url, withCredentials: true };

    if (data?.params) {
      requestConfig.params = data.params;
      requestConfig.paramsSerializer = (params) => qs.stringify(params, { arrayFormat: 'repeat' });
    }

    if (data?.body && method !== 'get') {
      if (config?.type === 'json') {
        requestConfig.data = data.body;
        requestConfig.headers = { 'Content-Type': 'application/json' };
      } else if (config?.type === 'file') {
        requestConfig.data = data.body;
        requestConfig.headers = { 'Content-Type': 'multipart/form-data' };
      } else {
        requestConfig.data = qs.stringify(data.body, { arrayFormat: 'repeat' });
      }
    }

    return axios(requestConfig);
  }, mutationOptions);

  const api: typeof mutation & {
    execute: typeof mutation.mutate;
    executeAsync: typeof mutation.mutateAsync;
  } = {
    ...mutation,
    execute: mutation.mutate,
    executeAsync: mutation.mutateAsync
  };

  return api;
}
