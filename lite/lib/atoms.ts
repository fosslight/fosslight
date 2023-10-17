import { atom } from 'recoil';

export const viewState = atom<'pc' | 'mobile' | 'none'>({ key: 'viewState', default: 'none' });

export const loadingState = atom({ key: 'loadingState', default: false });
