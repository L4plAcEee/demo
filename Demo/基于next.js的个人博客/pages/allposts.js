
import fs from 'fs';
import path from 'path';
import matter from 'gray-matter';
import Link from 'next/link';
import Head from 'next/head';
import Layout from '@/components/Layout';

export default function Allposts({ allPostsData }) {
  return (
    <Layout>
      <Head>
        <title>查看所有博客</title>
      </Head>
      <main>
        <h1>所有博客</h1>
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

    allPostsData.sort((a, b) => (a.date < b.date ? 1 : -1));


  return {
    props: {
      allPostsData
    }
  };
}