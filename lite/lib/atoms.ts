import { atom } from 'recoil';

export const userState = atom<{ name: string; email: string } | null>({
  key: 'userState',
  default: null
});

export const viewState = atom<'pc' | 'mobile' | 'none'>({ key: 'viewState', default: 'none' });

export const loadingState = atom({ key: 'loadingState', default: false });
