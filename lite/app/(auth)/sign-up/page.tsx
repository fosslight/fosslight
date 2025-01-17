'use client';

import { useAPI } from '@/lib/hooks';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';

export default function SignUp() {
  const [wait, setWait] = useState(false);
  const [divisions, setDivisions] = useState<{ cdDtlNo: string; cdDtlNm: string }[]>([]);
  const router = useRouter();
  const { register, handleSubmit } = useForm();

  // API for loading users
  const loadDivisionsRequest = useAPI('get', '/api/lite/divisions', {
    onSuccess: (res) => setDivisions(res.data)
  });

  // API for signing up
  const signUpRequest = useAPI('post', '/system/user/saveAjax', {
    onStart: () => setWait(true),
    onSuccess: (res) => {
      if (res.data.isValid === 'true') {
        alert('Successfully signed up');
        router.push('/sign-in');
      } else {
        alert('Failed in signing up');
      }
    },
    onFinish: () => setWait(false)
  });

  useEffect(() => {
    loadDivisionsRequest.execute({});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <form
      className="flex flex-col gap-y-6 p-6 bg-white rounded"
      onSubmit={handleSubmit((data) => {
        if (data.password1 !== data.password2) {
          alert('Check your password again');
          return;
        }

        signUpRequest.execute({
          body: {
            userId: data.id,
            userPw: data.password1,
            email: data.email,
            userName: data.name,
            division: data.division
          }
        });
      })}
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
          {...register('password1', { required: true })}
        />
      </div>
      <div className="flex flex-col gap-y-1">
        <div className="font-semibold">Password Confirm</div>
        <input
          className="w-full py-1 bg-transparent border-b border-b-semigray outline-none"
          type="password"
          {...register('password2', { required: true })}
        />
      </div>
      <div className="flex flex-col gap-y-1">
        <div className="font-semibold">Email</div>
        <input
          className="w-full py-1 bg-transparent border-b border-b-semigray outline-none"
          type="email"
          {...register('email', { required: true })}
        />
      </div>
      <div className="flex flex-col gap-y-1">
        <div className="font-semibold">Name</div>
        <input
          className="w-full py-1 bg-transparent border-b border-b-semigray outline-none"
          {...register('name', { required: true })}
        />
      </div>
      <div className="flex flex-col gap-y-1">
        <div className="font-semibold">Division</div>
        <select
          className="w-full py-1 bg-transparent border-b border-b-semigray outline-none"
          {...register('division')}
        >
          <option value="">(Select)</option>
          {divisions.map((division) => (
            <option key={division.cdDtlNo} value={division.cdDtlNo}>
              {division.cdDtlNm}
            </option>
          ))}
        </select>
      </div>
      <button className="w-full py-1 text-lg crimson-btn" disabled={wait}>
        Sign Up
      </button>
      <Link className="mx-auto text-sm text-darkgray" href="/sign-in">
        Already signed up? Sign in here.
      </Link>
    </form>
  );
}
