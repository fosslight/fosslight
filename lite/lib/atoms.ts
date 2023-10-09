import { atom } from 'recoil';

export const viewState = atom<'pc' | 'mobile' | 'none'>({ key: 'viewState', default: 'none' });
