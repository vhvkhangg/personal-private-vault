export default function GoogleDriveLoading() {
  return (
    <div className="flex h-full w-full items-center justify-center bg-slate-950 p-6 animate-pulse">
      <div className="flex flex-col gap-4 w-full max-w-5xl">
        <div className="h-8 w-48 rounded-lg bg-slate-800" />
        <div className="h-14 w-full rounded-xl bg-slate-900" />
        <div className="h-12 w-full rounded-xl bg-slate-900/60" />
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
          {Array.from({ length: 8 }).map((_, i) => (
            <div key={i} className="h-32 rounded-xl bg-slate-900/40" />
          ))}
        </div>
      </div>
    </div>
  );
}
