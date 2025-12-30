
import fs from 'fs';
import path from 'path';
import matter from 'gray-matter';
import Link from 'next/link';
import Head from 'next/head';
import Layout from '@/components/Layout';

export default function Home({ allPostsData }) {
  return (
    <Layout>
      <Head>
        <title>L4plAce的个人博客</title>
        <meta name="description" content="一些记录与迷思" />
      </Head>
      <main>
        <h1>欢迎来到我的博客</h1>
        <ul>
          {allPostsData.map(({ slug, title, date, description }) => (
            <li key={slug}>
              <h2>
                <Link href={`/posts/${slug}`}>
                  {title}
                </Link>
              </h2>
              <small>{date}</small>
              <p>{description}</p>
            </li>
          ))}
        </ul>
      </main>
    </Layout>
  );
}

export async function getStaticProps() {
  const postsDirectory = path.join(process.cwd(), 'posts');
  const filenames = fs.readdirSync(postsDirectory);

  const allPostsData = filenames.map((filename) => {
    const slug = filename.replace(/\.md$/, '');
    const fullPath = path.join(postsDirectory, filename);
    const fileContents = fs.readFileSync(fullPath, 'utf8');

    const matterResult = matter(fileContents);

    return {
      slug,
      title: matterResult.data.title,
      date: matterResult.data.date,
      description: matterResult.data.description,
    };
  });
  // allPostsData.sort((a, b) => new Date(b.date) - new Date(a.date));
  allPostsData.sort((a, b) => (a.date < b.date ? 1 : -1));
  const limitedPostsData = allPostsData.slice(0, 5);

  return {
    props: {
      allPostsData: limitedPostsData,
    }
  };
}
