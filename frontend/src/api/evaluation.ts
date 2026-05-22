import request from './request'
import type {
  MoralEvaluation,
  AcademicEvaluation,
  PhysicalEvaluation,
  ArtEvaluation,
  PracticeEvaluation,
  ComprehensiveEvaluation,
  EvaluationQuery,
  ComprehensiveDetailQuery,
} from '@/types/evaluation'

/* ── 德育 ── */
export const getMoralPage = (params: EvaluationQuery) =>
  request.get('/evaluation/moral/page', { params })

export const saveMoral = (data: Partial<MoralEvaluation>) =>
  request.post('/evaluation/moral', data)

export const deleteMoral = (id: number) =>
  request.delete(`/evaluation/moral/${id}`)

/* ── 智育 ── */
export const getAcademicPage = (params: EvaluationQuery) =>
  request.get('/evaluation/academic/page', { params })

export const saveAcademic = (data: Partial<AcademicEvaluation>) =>
  request.post('/evaluation/academic', data)

export const deleteAcademic = (id: number) =>
  request.delete(`/evaluation/academic/${id}`)

/* ── 体育 ── */
export const getPhysicalPage = (params: EvaluationQuery) =>
  request.get('/evaluation/physical/page', { params })

export const savePhysical = (data: Partial<PhysicalEvaluation>) =>
  request.post('/evaluation/physical', data)

export const deletePhysical = (id: number) =>
  request.delete(`/evaluation/physical/${id}`)

/* ── 美育 ── */
export const getArtPage = (params: EvaluationQuery) =>
  request.get('/evaluation/art/page', { params })

export const saveArt = (data: Partial<ArtEvaluation>) =>
  request.post('/evaluation/art', data)

export const deleteArt = (id: number) =>
  request.delete(`/evaluation/art/${id}`)

/* ── 劳动 ── */
export const getPracticePage = (params: EvaluationQuery) =>
  request.get('/evaluation/practice/page', { params })

export const savePractice = (data: Partial<PracticeEvaluation>) =>
  request.post('/evaluation/practice', data)

export const deletePractice = (id: number) =>
  request.delete(`/evaluation/practice/${id}`)

/* ── 综合评价（只读） ── */
export const getComprehensivePage = (params: EvaluationQuery) =>
  request.get('/evaluation/comprehensive/page', { params })

export const getComprehensiveDetail = (params: ComprehensiveDetailQuery) =>
  request.get('/evaluation/comprehensive/detail', { params })

export const getComprehensiveGrowth = (studentId: number) =>
  request.get(`/evaluation/comprehensive/growth/${studentId}`)

export const getMyComprehensive = () =>
  request.get('/evaluation/comprehensive/my')
