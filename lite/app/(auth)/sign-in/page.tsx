'use client';

import { useAPI } from '@/lib/hooks';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { useForm } from 'react-hook-form';

export default function SignIn() {
  const [wait, setWait] = useState(false);
  const router = useRouter();
  const { register, handleSubmit } = useForm();

  // API for signing in
  const signInRequest = useAPI('post', 'http://localhost:8180/session/login-proc', {
    onStart: () => setWait(true),
    onSuccess: (res) => {
      if (res.data.response.error) {
        alert(res.data.response.message);
      } else {
        router.push('/');
      }
    },
    onFinish: () => setWait(false)
  });

  return (
    <form
      className="flex flex-col gap-y-6 p-6 bg-white rounded"
      onSubmit={handleSubmit((data) =>
        signInRequest.execute({ body: { un: data.id, up: data.password } })
      )}
    >
      <div className="flex flex-col gap-y-1">
        <div className="font-semibold">ID</div>
        <input
          className="w-full py-1 bg-transparent border-b border-b-semigray outline-none"
          {...register('id', { required: true })}
        />
      </div>
      <div className="flex flex-col gap-y-1">
        <div className="font-semibold">Password</div>
        <input
          className="w-full py-1 bg-transparent border-b border-b-semigray outline-none"
          type="password"
          {...register('password', { required: true })}
        />
      </div>
      <div className="flex flex-col gap-y-2">
        <button className="w-full py-1 text-lg crimson-btn" disabled={wait}>
          Sign In
        </button>
        <Link className="w-full py-1 text-lg default-btn" href="/sign-up">
          Sign Up
        </Link>
      </div>
      <Link className="mx-auto text-sm text-darkgray" href="/reset-password">
        Forgot password? Click here.
      </Link>
    </form>
  );
}
