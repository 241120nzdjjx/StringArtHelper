const generator = require('./generator-core')

worker.onMessage((message) => {
  if (!message || message.type !== 'generate') return
  try {
    const result = generator.generate(message.options, {
      progress(complete, total) {
        worker.postMessage({ type: 'progress', complete, total })
      }
    })
    worker.postMessage({ type: 'result', result })
  } catch (error) {
    worker.postMessage({
      type: 'error',
      message: error && error.message ? error.message : String(error)
    })
  }
})
