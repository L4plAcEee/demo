
import fs from 'fs';
import path from 'path';
import matter from 'gray-matter';
import Link from 'next/link';
import Head from 'next/head';
import Layout from '@/components/Layout';

export default function Allreposts({ allPostsData }) {
    return (
      <Layout>
        <Head>
          <title>查看所有转载</title>
        </Head>
        <main>
          <h1>所有转载</h1>
          <ul>
            {allPostsData.map(({ slug, title, date, description, link }) => (
              <li key={slug}>
                <h2>
                  <Link href={`/reposts/${slug}`}>
                    {title}
                  </Link>
                </h2>
                <small>{date}</small>
                <p>{description}</p>

                <Link href={link}>
                  原帖地址
                </Link>
              </li>
            ))}
          </ul>
        </main>
      </Layout>
    );
  }
  
  export async function getStaticProps() {
    const postsDirectory = path.join(process.cwd(), 'reposts');
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
        link: matterResult.data.link,
      };
    });
  
      allPostsData.sort((a, b) => (a.date < b.date ? 1 : -1));
  
  
    return {
      props: {
        allPostsData
      }
    };
  }