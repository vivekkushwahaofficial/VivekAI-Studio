import ReactMarkdown from 'react-markdown';
import { CodeBlock } from './CodeBlock';

export const MarkdownRenderer = ({ content }) => {
  return (
    <div className="prose prose-invert max-w-none text-slate-300 leading-relaxed text-sm space-y-3">
      <ReactMarkdown
        components={{
          code({ node, className, children, ...props }) {
            const match = /language-(\w+)/.exec(className || '');
            const isInline = !match;
            
            if (isInline) {
              return (
                <code className="px-1.5 py-0.5 rounded bg-slate-900 border border-slate-800 text-cyan-400 font-mono text-xs" {...props}>
                  {children}
                </code>
              );
            }
            
            return (
              <CodeBlock
                language={match[1]}
                value={String(children).replace(/\n$/, '')}
              />
            );
          },
        }}
      >
        {content}
      </ReactMarkdown>
    </div>
  );
};
