import Giscus from '@giscus/react';

export default function Comments() {
  return (
    <Giscus
      repo="L4plAcEee/blog4l"
      repoId="R_kgDONqgf8Q"
      category="Announcements"
      categoryId="DIC_kwDONqgf8c4CmBV2"
      mapping="url"
      reactionsEnabled="1"
      emitMetadata="1"
      inputPosition="top"
      theme="preferred_color_scheme"
      lang="zh-CN"
      loading="lazy"
      crossorigin="anonymous"
    />
  );
}