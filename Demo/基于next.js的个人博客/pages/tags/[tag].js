

import fs from 'fs';
import path from 'path';
import matter from 'gray-matter';
import Link from 'next/link';
import Layout from '@/components/Layout';
import Head from 'next/head';

export default function TagPage({ tag, posts }) {
  return (
    <Layout>
      <Head>
        <title>标签: {tag} | 我的个人博客</title>
        <meta name="description" content={`查看所有关于 ${tag} 的文章。`} />
      </Head>
      <main>
        <h1>标签: {tag}</h1>
        <ul>
          {posts.map(({ slug, title, date, description }) => (
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

export async function getStaticPaths() {
  const postsDirectory = path.join(process.cwd(), 'posts');
  const filenames = fs.readdirSync(postsDirectory);
  const tags = new Set();

  filenames.forEach((filename) => {
    const fullPath = path.join(postsDirectory, filename);
    const fileContents = fs.readFileSync(fullPath, 'utf8');
    const { data } = matter(fileContents);
    if (data.tags) {
      data.tags.forEach((tag) => tags.add(tag));
    }
  });

  const paths = Array.from(tags).map((tag) => ({
    params: { tag },
  }));

  return { paths, fallback: false };
}

export async function getStaticProps({ params }) {
  const postsDirectory = path.join(process.cwd(), 'posts');
  const filenames = fs.readdirSync(postsDirectory);
  const tag = params.tag;
  const posts = filenames
    .map((filename) => {
      const slug = filename.replace(/\.md$/, '');
      const fullPath = path.join(postsDirectory, filename);
      const fileContents = fs.readFileSync(fullPath, 'utf8');
      const { data } = matter(fileContents);
      if (data.tags && data.tags.includes(tag)) {
        return {
          slug,
          title: data.title,
          date: data.date,
          description: data.description,
        };
      }
      return null;
    })
    .filter(Boolean);

  return {
    props: {
      tag,
      posts,
    },
  };
}
