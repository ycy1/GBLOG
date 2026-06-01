// composables/useSSE.ts
import { ref } from 'vue';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import { getToken } from '@/utils/auth'

export function useSSE() {
  const data = ref('');        // 存储流式数据
  const isStreaming = ref(false); // 连接状态
  let abortController: AbortController | null = null;  // 用于主动断开连接

  const start = async (url: string, options: any, onData: (data: string) => void) => {
    // 核心：每次请求必须新建 AbortController，因为它是“一次性”的 [citation:4]
    abortController = new AbortController();
    isStreaming.value = true;
    data.value = ''; // 清空上次数据
    const token = getToken()
    try {
      await fetchEventSource(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': token ?? ''
        },
        body: JSON.stringify(options.body),
        signal: abortController.signal,
        openWhenHidden: true, // 解决页面切后台时的重复请求问题 [citation:1]

        onmessage(event) {
          // 累积数据，实现打字机效果 [citation:5]
          // console.log(event.data);
          onData(event.data);
          data.value += event.data;
          
        },

        onerror(err) {
          console.error('SSE错误', err);
          isStreaming.value = false;
          abortController?.abort(); // 出错时断开
          throw err; // 抛出错误，让调用者处理
        },

        onclose() {
          isStreaming.value = false;
        }
      });
    } catch (error) {
      if (error instanceof Error && error.name === 'AbortError') {
        console.log('主动中止请求');
      } else {
        console.error('连接失败', error);
      }
      isStreaming.value = false;
    }
  };

  const stop = () => {
    if (abortController) {
      abortController.abort(); // 主动关闭连接 
      isStreaming.value = false;
    }
  };

  return { aiData:data, isStreaming, start, stop };
}