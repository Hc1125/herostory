package org.tinygame.herostory.rank;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.async.AsyncOperationProcessor;
import org.tinygame.herostory.async.IAsyncOperation;
import org.tinygame.herostory.util.RedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * 排行榜服务
 */
public final class RankService {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RankService.class);

    /**
     * 单例对象
     */
    private static final RankService _instance = new RankService();

    /**
     * 私有化类默认构造器
     */
    private RankService() {}

    /**
     * 获取单例对象
     *
     * @return 单例对象
     */
    public static RankService getInstance() {
        return _instance;
    }

    /**
     * 获取排行榜
     *
     * @param callback 回调函数
     */
    public void getRank(Function<List<RankItem>, Void> callback) {
        if (null == callback) {
            return;
        }

        AsyncOperationProcessor.getInstance().process(new AsyncGetRank(){
            @Override
            public void doFinish() {
                callback.apply(this.getRankItemList());
            }
        });
    }

    /**
     * 刷新排行榜
     * 改变排行榜规则改变refreshRank即可
     * 然后重启RankApp，不用重启游戏服务器
     *
     * @param winnerId 赢家 Id
     * @param loserId 输家 Id
     */
    public void refreshRank(int winnerId, int loserId) {
        if (winnerId <= 0 || loserId <= 0) {
            return;
        }
        try (Jedis redis = RedisUtil.getJedis()) {
            // 增加用户的胜利和失败次数
            redis.hincrBy("User_" + winnerId, "Win", 1);
            redis.hincrBy("User_" + loserId, "Lose", 1);

            // 看看赢家总共赢了多少次?
            String winStr = redis.hget("User_" + winnerId, "Win");
            int winNum = Integer.parseInt(winStr);

            // 修改排名数据
            redis.zadd("Rank", winNum, String.valueOf(winnerId));
        } catch (Exception e) {
            // 记录错误日志
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 异步方式获取排行榜
     */
    private static class AsyncGetRank implements IAsyncOperation {
        /**
         * 排名条目列表
         */
        private List<RankItem> _rankItemList;

        /**
         * 获取排名条目列表
         *
         * @return 排名条目列表
         */
        List<RankItem> getRankItemList() {
            return _rankItemList;
        }

        @Override
        public void doAsync() {
            try (Jedis redis = RedisUtil.getJedis()) {
                // 获取集合字符串
                Set<Tuple> valSet = redis.zrevrangeWithScores("Rank", 0, 9);

                List<RankItem> rankItemList = new ArrayList<>();
                int i = 0;

                for (Tuple t : valSet) {
                    if (null == t) {
                        continue;
                    }

                    // 获取用户Id
                    int userId = Integer.parseInt(t.getElement());

                    // 获取用户信息
                    String jsonStr = redis.hget("User_" + userId, "BasicInfo");

                    if (null == jsonStr) {
                        continue;
                    }

                    // 创建排名条目
                    RankItem newItem = new RankItem();
                    newItem.rankId = ++i;
                    newItem.userId = userId;
                    newItem.win = (int) t.getScore();

                    JSONObject jsonObj = JSONObject.parseObject(jsonStr);
                    newItem.userName = jsonObj.getString("userName");
                    newItem.heroAvatar = jsonObj.getString("heroAvatar");

                    rankItemList.add(newItem);
                }

                _rankItemList = rankItemList;
            } catch (Exception e) {
                // 记录错误日志
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

}
