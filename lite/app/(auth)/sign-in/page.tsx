import Link from 'next/link';

export default function SignIn() {
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
        <button className="w-full py-1 text-lg crimson-btn">Sign In</button>
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
