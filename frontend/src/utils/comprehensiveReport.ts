import type { ComprehensiveEvaluation } from '../types/evaluation'

export interface ReportDimensionSuggestion {
  dimKey: string
  dimLabel: string
  level?: string
  levelLabel: string
  score: number
  rank: number
  total: number
  suggestion: string
}

export interface ComprehensiveSuggestion {
  summary?: string
  strengths?: ReportDimensionSuggestion[]
  weaknesses?: ReportDimensionSuggestion[]
  actionItems?: string[]
  typicality?: {
    label: string
    description: string
  }
}

const dimensions = [
  { label: '德育', key: 'moralScore' },
  { label: '智育', key: 'academicScore' },
  { label: '体育', key: 'physicalScore' },
  { label: '美育', key: 'artScore' },
  { label: '劳动', key: 'practiceScore' },
] as const

function escapeHtml(value: unknown) {
  return String(value ?? '-')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function renderDimensionTable(detail: ComprehensiveEvaluation) {
  return dimensions.map(dim => {
    const score = detail[dim.key]
    return `
      <tr>
        <td>${dim.label}</td>
        <td>${escapeHtml(score)}</td>
        <td><div class="bar"><span style="width: ${Math.min(Number(score) || 0, 100)}%"></span></div></td>
      </tr>
    `
  }).join('')
}

function renderDimensionSuggestions(title: string, items?: ReportDimensionSuggestion[]) {
  if (!items?.length) return ''
  return `
    <section>
      <h2>${title}</h2>
      ${items.map(item => `
        <div class="suggestion-card">
          <div class="card-title">
            <strong>${escapeHtml(item.dimLabel)}</strong>
            <span>${escapeHtml(item.levelLabel)} · ${escapeHtml(item.score)} 分 · 排名 ${escapeHtml(item.rank)}/${escapeHtml(item.total)}</span>
          </div>
          <p>${escapeHtml(item.suggestion)}</p>
        </div>
      `).join('')}
    </section>
  `
}

function renderActionItems(items?: string[]) {
  if (!items?.length) return ''
  return `
    <section>
      <h2>行动建议</h2>
      <ol>
        ${items.map(item => `<li>${escapeHtml(item)}</li>`).join('')}
      </ol>
    </section>
  `
}

function renderTypicality(suggestion?: ComprehensiveSuggestion | null) {
  if (!suggestion?.typicality) return ''
  return `
    <section>
      <h2>群体特征</h2>
      <p><strong>${escapeHtml(suggestion.typicality.label)}</strong>：${escapeHtml(suggestion.typicality.description)}</p>
    </section>
  `
}

export function buildComprehensiveReportHtml(
  detail: ComprehensiveEvaluation,
  suggestion?: ComprehensiveSuggestion | null,
) {
  const title = `${detail.studentName || '学生'}综合素质报告`
  return `<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8" />
  <title>${escapeHtml(title)}</title>
  <style>
    * { box-sizing: border-box; }
    body {
      margin: 0;
      color: #1f2933;
      font-family: "Microsoft YaHei", "PingFang SC", Arial, sans-serif;
      background: #f5f7fa;
    }
    .report {
      width: 210mm;
      min-height: 297mm;
      margin: 0 auto;
      padding: 22mm 20mm;
      background: #fff;
    }
    h1 {
      margin: 0 0 6mm;
      text-align: center;
      font-size: 24px;
      letter-spacing: 0;
    }
    .subtitle {
      margin: 0 0 12mm;
      text-align: center;
      color: #64748b;
      font-size: 13px;
    }
    section { margin-top: 10mm; break-inside: avoid; }
    h2 {
      margin: 0 0 4mm;
      padding-left: 3mm;
      border-left: 4px solid #1f9d8a;
      font-size: 17px;
    }
    .info-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 4mm 8mm;
      padding: 6mm;
      border: 1px solid #d8e0e8;
      border-radius: 4px;
      background: #fbfcfd;
    }
    .label { display: block; color: #64748b; font-size: 12px; }
    .value { display: block; margin-top: 1mm; font-size: 15px; font-weight: 600; }
    .score { color: #087f6f; font-size: 20px; }
    table { width: 100%; border-collapse: collapse; }
    th, td { padding: 3.5mm; border: 1px solid #d8e0e8; text-align: left; }
    th { background: #f0f5f4; }
    .bar { height: 8px; overflow: hidden; border-radius: 999px; background: #e2e8f0; }
    .bar span { display: block; height: 100%; border-radius: 999px; background: #1f9d8a; }
    .summary, .suggestion-card {
      padding: 4mm;
      border: 1px solid #d8e0e8;
      border-radius: 4px;
      background: #fbfcfd;
      line-height: 1.7;
    }
    .card-title {
      display: flex;
      justify-content: space-between;
      gap: 8mm;
      color: #475569;
    }
    .card-title strong { color: #1f2933; }
    .suggestion-card + .suggestion-card { margin-top: 3mm; }
    ol { margin: 0; padding-left: 7mm; line-height: 1.8; }
    @page { size: A4; margin: 0; }
    @media print {
      body { background: #fff; }
      .report { width: auto; min-height: auto; margin: 0; box-shadow: none; }
    }
  </style>
</head>
<body>
  <main class="report">
    <h1>学生综合素质报告</h1>
    <p class="subtitle">学年：${escapeHtml(detail.academicYear)}</p>

    <section>
      <h2>基本信息</h2>
      <div class="info-grid">
        <div><span class="label">姓名</span><span class="value">${escapeHtml(detail.studentName)}</span></div>
        <div><span class="label">学号</span><span class="value">${escapeHtml(detail.studentNo)}</span></div>
        <div><span class="label">班级</span><span class="value">${escapeHtml(detail.className)}</span></div>
        <div><span class="label">综合总分</span><span class="value score">${escapeHtml(detail.totalScore)}</span></div>
        <div><span class="label">聚类分组</span><span class="value">${escapeHtml(detail.clusterName || '-')}</span></div>
        <div><span class="label">生成时间</span><span class="value">${escapeHtml(new Date().toLocaleString('zh-CN'))}</span></div>
      </div>
    </section>

    <section>
      <h2>五维评价得分</h2>
      <table>
        <thead><tr><th>评价维度</th><th>得分</th><th>表现</th></tr></thead>
        <tbody>${renderDimensionTable(detail)}</tbody>
      </table>
    </section>

    ${suggestion?.summary ? `
      <section>
        <h2>综合评语</h2>
        <p class="summary">${escapeHtml(suggestion.summary)}</p>
      </section>
    ` : ''}
    ${renderDimensionSuggestions('优势维度', suggestion?.strengths)}
    ${renderDimensionSuggestions('待提升维度', suggestion?.weaknesses)}
    ${renderActionItems(suggestion?.actionItems)}
    ${renderTypicality(suggestion)}
  </main>
</body>
</html>`
}

export function printComprehensiveReport(
  detail: ComprehensiveEvaluation,
  suggestion?: ComprehensiveSuggestion | null,
) {
  const reportWindow = window.open('', '_blank')
  if (!reportWindow) return false

  reportWindow.document.open()
  reportWindow.document.write(buildComprehensiveReportHtml(detail, suggestion))
  reportWindow.document.close()
  reportWindow.focus()
  reportWindow.print()
  return true
}
