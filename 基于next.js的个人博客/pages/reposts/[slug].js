import fs from 'fs';
import path from 'path';
import matter from 'gray-matter';
import { remark } from 'remark';
import html from 'remark-html';
import Head from 'next/head';
import Image from 'next/image';
import Link from 'next/link';
import Layout from '@/components/Layout';
import styles from '@/styles/post.module.css';
import Comments from '@/components/Comments';

export default function Reposts({ postData, relatedPosts = [] }) {
  const jsonLd = {
    "@context": "https://schema.org",
    "@type": "BlogPosting",
    "headline": postData.title,
    "description": postData.description,
    "image": postData.coverImage ? `https://l4place.com${postData.coverImage}` : `https://l4place.com/default-og-image.jpg`,
    "author": {
      "@type": "Person",
      "name": "l4place"
    },
    "datePublished": postData.date,
    "mainEntityOfPage": {
      "@type": "WebPage",
      "@id": `https://l4place.com/posts/${postData.slug}`
    }
  };

  return (
    <Layout>
      <Head>
        <title>{postData.title} | 我的个人博客</title>
        <meta name="description" content={postData.description} />
        <meta property="og:title" content={postData.title} />
        <meta property="og:description" content={postData.description} />
        <meta property="og:type" content="article" />
        <meta property="og:image" content={postData.coverImage || '/default-og-image.jpg'} />
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
        />
      </Head>
      <article className={styles.article}>
        <h1>{postData.title}</h1>
        {postData.coverImage && (
          <Image
            src={postData.coverImage}
            alt={postData.title}
            width={800}
            height={400}
            layout="responsive"
          />
        )}
        <div dangerouslySetInnerHTML={{ __html: postData.contentHtml }} />
        <div className="tags">
          {postData.tags && postData.tags.map((tag) => (
            <Link key={tag} href={`/tags/${tag}`}>
              {tag}
            </Link>
          ))}
        </div>
      </article>

      {/* 显示相关文章 */}
      {relatedPosts.length > 0 && (
        <div className={styles.relatedPosts}>
          <h2>相关文章</h2>
          <ul>
            {relatedPosts.map((post) => (
              <li key={post.slug}>
                <Link href={`/reposts/${post.slug}`}>
                  {post.title}
                </Link>
              </li>
            ))}
          </ul>
        </div>
      )}

      <Comments />
    </Layout>
  );
}

export async function getStaticPaths() {
  const postsDirectory = path.join(process.cwd(), 'reposts');
  const filenames = fs.readdirSync(postsDirectory);

  const paths = filenames.map((filename) => {
    const slug = filename.replace(/\.md$/, '');
    return {
      params: {
        slug,
      },
    };
  });

  return {
    paths,
    fallback: false, // 所有路径在构建时生成
  };
}

export async function getStaticProps({ params }) {
  const postsDirectory = path.join(process.cwd(), 'reposts');
  const filenames = fs.readdirSync(postsDirectory);
  const fullPath = path.join(postsDirectory, `${params.slug}.md`);
  const fileContents = fs.readFileSync(fullPath, 'utf8');

  const matterResult = matter(fileContents);

  const processedContent = await remark()
    .use(html)
    .process(matterResult.content);
  const contentHtml = processedContent.toString();

  // 计算相关帖子
  const relatedPosts = filenames
    .map((filename) => {
      const slug = filename.replace(/\.md$/, '');
      const fullPath = path.join(postsDirectory, filename);
      const fileContents = fs.readFileSync(fullPath, 'utf8');
      const { data } = matter(fileContents);
      return {
        slug,
        title: data.title,
        date: data.date.toString(),
        description: data.description,
        tags: data.tags || [],
      };
    })
    .filter(
      (post) =>
        post.slug !== params.slug &&
        post.tags.some((tag) => matterResult.data.tags.includes(tag))
    );

  return {
    props: {
      postData: {
        slug: params.slug,
        ...matterResult.data,
        contentHtml,
      },
      relatedPosts,
    },
  };
}