/* ── 五维评价公共基础字段 ── */
interface EvaluationBase {
  id?: number
  studentId: number
  academicYear: string
  totalScore?: number
  remark?: string
  evaluatorId?: number
  createTime?: string
  updateTime?: string
  studentName?: string
  studentNo?: string
  evaluatorName?: string
}

/* ── 德育测评 ── */
export interface MoralEvaluation extends EvaluationBase {
  moralPracticeBase?: number
  politicalThought?: number
  integrity?: number
  learningAttitude?: number
  discipline?: number
  collective?: number
  civility?: number
  moralCharacterSubtotal?: number
  moralHonor?: number
  moralSocialWork?: number
  moralOutstanding?: number
  moralBonusSubtotal?: number
  moralDeduction?: number
}

/* ── 智育测评 ── */
export interface AcademicEvaluation extends EvaluationBase {
  weightedAvgScore?: number
  academicBonus?: number
  academicOther?: number
  academicBonusSubtotal?: number
  academicDeduction?: number
}

/* ── 体育测评 ── */
export interface PhysicalEvaluation extends EvaluationBase {
  physicalPerformance?: number
  physicalBonus?: number
  physicalDeduction?: number
}

/* ── 美育测评 ── */
export interface ArtEvaluation extends EvaluationBase {
  artPerformance?: number
  artBonus?: number
  artDeduction?: number
}

/* ── 劳动教育测评 ── */
export interface PracticeEvaluation extends EvaluationBase {
  laborPerformance?: number
  laborBonus?: number
  laborDeduction?: number
}

/* ── 综合素质评价汇总（只读） ── */
export interface ComprehensiveEvaluation {
  id: number
  studentId: number
  academicYear: string
  moralScore: number
  academicScore: number
  physicalScore: number
  artScore: number
  practiceScore: number
  totalScore: number
  clusterLabel: number
  clusterName: string
  suggestion: string
  createTime: string
  updateTime: string
  studentName: string
  studentNo: string
  className: string
}

/* ── 评价查询参数 ── */
export interface EvaluationQuery {
  current: number
  size: number
  studentId?: number
  academicYear?: string
  keyword?: string
  classId?: number
  clusterLabel?: number
}

/* ── 综合评价详情查询 ── */
export interface ComprehensiveDetailQuery {
  studentId: number
  academicYear: string
}
