import { nodeResolve } from '@rollup/plugin-node-resolve';
import terser from '@rollup/plugin-terser';
import { createRequire } from 'node:module';
const require = createRequire(import.meta.url);
const pkg = require('./package.json');
import { spawn } from 'child_process';

const ls = spawn('git', ['describe', '--always', '--abbrev=8', '--match', 'NOT A TAG', '--dirty=*']);
var commit;
ls.stdout.on('data', (data) => { commit = data; });

const input = ['src/unet.js'];
export default [
  {
    // UMD
    input,
    plugins: [
      nodeResolve(),
    ],
    output: [{
      file: `dist/${pkg.name}.js`,
      format: 'umd',
      name: 'unet',
      esModule: false,
      exports: 'named',
      sourcemap: true,
    },{
      file: `dist/${pkg.name}.min.js`,
      format: 'umd',
      name: 'unet',
      esModule: false,
      exports: 'named',
      sourcemap: true,
      plugins: [terser()]
    }],
  },
  // ESM and CJS
  {
    input,
    plugins: [nodeResolve()],
    output: [
      {
        format: 'esm',
        exports: 'named',
        dir : 'dist/esm',
        chunkFileNames: '[name].js',
        banner : `/* unet.js v${pkg.version}${commit?'/'+commit:''} ${new Date().toISOString()} */\n`
      },
      {
        format: 'cjs',
        exports: 'named',
        dir : 'dist/cjs',
        chunkFileNames: '[name].cjs',
        entryFileNames: '[name].cjs',
        banner : `/* unet.js v${pkg.version}${commit?'/'+commit:''} ${new Date().toISOString()} */\n`
      },
    ],
  }
];