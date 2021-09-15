const isProduction = process.env.NODE_ENV === 'production'

module.exports = {
  mode: isProduction ? null : "jit",
  purge: [
    './src/client/**/*.cljs',
    './src/client/**/*.js'
  ],
  darkMode: true, // or 'media' or 'class'
  theme: {
    extend: {},
  },
  variants: {
    extend: {},
  },
  plugins: [],
}
