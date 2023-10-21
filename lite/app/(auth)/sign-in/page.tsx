'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';

export default function SignIn() {
  const router = useRouter();

  return (
    <form className="flex flex-col gap-y-6 p-6 bg-white rounded">
      <div className="flex flex-col gap-y-1">
        <div className="font-semibold">ID</div>
        <input className="w-full py-1 bg-transparent border-b border-b-semigray outline-none" />
      </div>
      <div className="flex flex-col gap-y-1">
        <div className="font-semibold">Password</div>
        <input
          className="w-full py-1 bg-transparent border-b border-b-semigray outline-none"
          type="password"
        />
      </div>
      <label className="flex items-center">
        <input className="w-4 h-4" type="checkbox" />
        &ensp;
        <span className="font-semibold">Save ID</span>
      </label>
      <div className="flex flex-col gap-y-2">
        <button className="w-full py-1 bg-crimson border border-crimson rounded text-lg text-semiwhite">
          Sign In
        </button>
        <button
          className="w-full py-1 bg-transparent border border-gray rounded text-lg"
          type="button"
          onClick={() => router.push('/sign-up')}
        >
          Sign Up
        </button>
      </div>
      <Link className="mx-auto text-sm text-darkgray" href="/reset-password">
        Forgot password? Click here.
      </Link>
    </form>
  );
}
