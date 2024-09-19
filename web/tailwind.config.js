/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./src/**/*.{html,js,vue}",
  ],
  theme: {
    extend: {
      screens:{
        'md': '2000px', // 自定义断点，用于匹配宽度大于2400px的情况
      },
      colors:{
      }
    },
  },
  plugins: [],
}

