
import Link from 'next/link';

export default function Header() {
  return (
    <header className="py-4 border-b">
      <nav className="flex justify-between items-center max-w-4xl mx-auto">
        <Link href="/" className="text-blue-500 hover:underline">
          首页 
        </Link>
        <>  </>
        <Link href="/allposts" className="text-blue-500 hover:underline">
          所有文章
        </Link>
        <>  </>
        <Link href="/allreposts" className="text-blue-500 hover:underline">
          转载文章
        </Link>
        <>  </>
        <Link href="/tools" className="text-blue-500 hover:underline">
          小工具
        </Link>
        <>  </>
        <Link href="/about" className="text-blue-500 hover:underline">
          关于我 
        </Link>
      </nav>
    </header>
  );
}
