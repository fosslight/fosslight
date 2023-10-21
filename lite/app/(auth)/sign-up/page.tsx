import Link from 'next/link';

export default function SignUp() {
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
      <div className="flex flex-col gap-y-1">
        <div className="font-semibold">Password Confirm</div>
        <input
          className="w-full py-1 bg-transparent border-b border-b-semigray outline-none"
          type="password"
        />
      </div>
      <div className="flex flex-col gap-y-1">
        <div className="font-semibold">Email</div>
        <input
          className="w-full py-1 bg-transparent border-b border-b-semigray outline-none"
          type="email"
        />
      </div>
      <div className="flex flex-col gap-y-1">
        <div className="font-semibold">Name</div>
        <input className="w-full py-1 bg-transparent border-b border-b-semigray outline-none" />
      </div>
      <div className="flex flex-col gap-y-1">
        <div className="font-semibold">Division</div>
        <select className="w-full py-1 bg-transparent border-b border-b-semigray outline-none">
          <option>SW Lab</option>
          <option>Open Source Task</option>
          <option>AI Lab</option>
          <option>TESTGG</option>
          <option>N/A</option>
        </select>
      </div>
      <button className="w-full py-1 bg-crimson border border-crimson rounded text-lg text-semiwhite">
        Sign Up
      </button>
      <Link className="mx-auto text-sm text-darkgray" href="/sign-in">
        Already signed up? Sign in here.
      </Link>
    </form>
  );
}
