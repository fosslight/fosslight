export default function Layout({ children }: { children: React.ReactNode }) {
  return (
    <main>
      <div>Side Bar and Full Search Bar</div>
      {children}
    </main>
  );
}
