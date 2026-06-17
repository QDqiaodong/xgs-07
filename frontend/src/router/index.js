import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/manuscripts',
    name: 'Manuscripts',
    component: () => import('@/views/ManuscriptList.vue')
  },
  {
    path: '/manuscript/:id',
    name: 'ManuscriptDetail',
    component: () => import('@/views/ManuscriptDetail.vue')
  },
  {
    path: '/create',
    name: 'CreateManuscript',
    component: () => import('@/views/CreateManuscript.vue')
  },
  {
    path: '/edit/:id',
    name: 'EditManuscript',
    component: () => import('@/views/CreateManuscript.vue')
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('@/views/Favorites.vue')
  },
  {
    path: '/notes',
    name: 'Notes',
    component: () => import('@/views/Notes.vue')
  },
  {
    path: '/author/:name',
    name: 'AuthorProfile',
    component: () => import('@/views/AuthorProfile.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
