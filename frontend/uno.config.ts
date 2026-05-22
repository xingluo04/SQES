import { defineConfig, presetUno, presetIcons, transformerDirectives } from 'unocss'

export default defineConfig({
  presets: [
    presetUno(),
    presetIcons({
      scale: 1.2,
      warn: true,
    }),
  ],
  transformers: [transformerDirectives()],
  theme: {
    colors: {
      'bg-primary': '#1B2838',
      'bg-secondary': '#243447',
      'bg-sidebar': '#151E2A',
      'bg-hover': '#2A3F55',
      'jade': '#4DB6AC',
      'jade-light': '#80CBC4',
      'vermilion': '#E57373',
      'amber': '#FFB74D',
      'text-primary': '#E8E0D8',
      'text-secondary': '#9CA8B7',
      'border-dark': '#2A3A4A',
    },
  },
})
