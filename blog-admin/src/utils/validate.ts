
import dayjs from 'dayjs'

const validate = {
    /**
   * 判断是否为外部链接
   * @param {string} path
   * @returns {boolean}
   */
  isExternal(path: string): boolean {
    return /^(https?:|mailto:|tel:)/.test(path)
  },

  /**
   * 时间转换为指定格式
   * @param {string} time
   * @param {string} format
   * @returns {string}
   */ 
  formatTime(time: string, format: string = 'YYYY-MM-DD HH:mm:ss'): string {
    return dayjs(time).format(format)
  }
}
// 导出工具类
export default validate
