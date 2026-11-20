import axios, { type AxiosError } from 'axios'
import type { ProblemDetail } from './types'

/**
 * An error response of our backend. It carries the ProblemDetail, so a page can react to
 * a specific case, for example problem.title === 'Seat already taken'.
 */
export class ApiError extends Error {
  readonly problem: ProblemDetail

  constructor(problem: ProblemDetail) {
    super(problem.detail ?? problem.title)
    this.name = 'ApiError'
    this.problem = problem
  }
}

// All calls go to /api: in development the Vite proxy forwards them, in the container Spring Boot answers itself.
export const http = axios.create({ baseURL: '/api' })

// Turn error responses of the backend into an ApiError. Network errors (backend not reachable) stay as they are.
http.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ProblemDetail>) => {
    const problem = error.response?.data
    if (problem && typeof problem === 'object' && 'status' in problem) {
      return Promise.reject(new ApiError(problem))
    }
    return Promise.reject(error)
  },
)
