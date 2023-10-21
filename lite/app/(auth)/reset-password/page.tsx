import Link from 'next/link';

export default function ResetPassword() {
  return (
    <form className="flex flex-col gap-y-6 p-6 bg-white rounded">
      <div className="flex flex-col gap-y-1">
        <div className="font-semibold">ID</div>
        <input className="w-full py-1 bg-transparent border-b border-b-semigray outline-none" />
      </div>
      <div className="flex flex-col gap-y-1">
        <div className="font-semibold">Email</div>
        <input
          className="w-full py-1 bg-transparent border-b border-b-semigray outline-none"
          type="email"
        />
      </div>
      <button className="w-full py-1 text-lg crimson-btn">Reset Password</button>
      <Link className="mx-auto text-sm text-darkgray" href="/sign-in">
        Return to sign in.
      </Link>
    </form>
  );
}
