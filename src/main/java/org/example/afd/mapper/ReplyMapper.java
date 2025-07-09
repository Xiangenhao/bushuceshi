package org.example.afd.mapper;

import org.apache.ibatis.annotations.*;
import org.example.afd.pojo.Reply;
import org.example.afd.pojo.ReplyLike;
import org.example.afd.dto.ReplyDTO;

import java.util.List;

@Mapper
public interface ReplyMapper {
    /**
     * 插入新回复
     */
    @Insert("INSERT INTO post_comment (user_id, post_id, parent_id, root_id, content, like_count, reply_count, create_time, status) " +
            "VALUES (#{userId}, #{postId}, #{parentId}, #{rootId}, #{content}, 0, 0, #{createTime}, 1)")
    @Options(useGeneratedKeys = true, keyProperty = "replyId", keyColumn = "comment_id")
    void insertReply(Reply reply);

    /**
     * 获取评论的回复列表
     */
    @Select("SELECT r.comment_id, r.parent_id, r.root_id, r.user_id, r.content, r.like_count, r.create_time, " +
            "u.username, u.avatar FROM post_comment r " +
            "JOIN users u ON r.user_id = u.user_id " +
            "WHERE r.root_id = #{commentId} AND r.status = 1 " +
            "ORDER BY r.create_time DESC " +
            "LIMIT #{offset}, #{size}")
    @Results({
        @Result(property = "replyId", column = "comment_id"),
        @Result(property = "parentId", column = "parent_id"),
        @Result(property = "rootId", column = "root_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "likeCount", column = "like_count"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "username", column = "username"),
        @Result(property = "avatar", column = "avatar")
    })
    List<ReplyDTO> getRepliesByCommentId(@Param("commentId") Long commentId, @Param("offset") Integer offset, @Param("size") Integer size);

    /**
     * 获取回复详情
     */
    @Select("SELECT r.comment_id, r.parent_id, r.root_id, r.user_id, r.content, r.like_count, r.create_time, " +
            "u.username, u.avatar FROM post_comment r " +
            "JOIN users u ON r.user_id = u.user_id " +
            "WHERE r.comment_id = #{replyId} AND r.status = 1")
    @Results({
        @Result(property = "replyId", column = "comment_id"),
        @Result(property = "parentId", column = "parent_id"),
        @Result(property = "rootId", column = "root_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "likeCount", column = "like_count"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "username", column = "username"),
        @Result(property = "avatar", column = "avatar")
    })
    ReplyDTO getReplyById(Long replyId);

    /**
     * 获取评论的回复列表，包含点赞状态
     * 修复：使用root_id获取所有层级的回复，而不仅仅是直接回复
     */
    @Select("SELECT r.comment_id, r.parent_id, r.root_id, r.user_id, r.content, r.like_count, r.create_time, " +
            "u.username, u.avatar, " +
            "IF(rl.id IS NULL, 0, 1) AS is_liked " +
            "FROM post_comment r " +
            "JOIN users u ON r.user_id = u.user_id " +
            "LEFT JOIN post_comment_like rl ON r.comment_id = rl.comment_id AND rl.user_id = #{userId} AND rl.status = 1 " +
            "WHERE r.root_id = #{commentId} AND r.status = 1 " +
            "ORDER BY r.create_time ASC " +
            "LIMIT #{offset}, #{size}")
    @Results({
        @Result(property = "replyId", column = "comment_id"),
        @Result(property = "parentId", column = "parent_id"),
        @Result(property = "rootId", column = "root_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "likeCount", column = "like_count"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "username", column = "username"),
        @Result(property = "avatar", column = "avatar"),
        @Result(property = "isLiked", column = "is_liked")
    })
    List<ReplyDTO> getRepliesWithLikeStatus(@Param("commentId") Long commentId, @Param("userId") Integer userId, @Param("offset") Integer offset, @Param("size") Integer size);

    /**
     * 获取回复详情，包含点赞状态
     */
    @Select("SELECT r.comment_id, r.parent_id, r.root_id, r.user_id, r.content, r.like_count, r.create_time, " +
            "r.status, u.username, u.avatar, " +
            "IF(rl.id IS NULL, 0, 1) AS is_liked " +
            "FROM post_comment r " +
            "JOIN users u ON r.user_id = u.user_id " +
            "LEFT JOIN post_comment_like rl ON r.comment_id = rl.comment_id AND rl.user_id = #{userId} AND rl.status = 1 " +
            "WHERE r.comment_id = #{replyId} AND r.status = 1")
    @Results({
        @Result(property = "replyId", column = "comment_id"),
        @Result(property = "parentId", column = "parent_id"),
        @Result(property = "rootId", column = "root_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "likeCount", column = "like_count"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "status", column = "status"),
        @Result(property = "username", column = "username"),
        @Result(property = "avatar", column = "avatar"),
        @Result(property = "isLiked", column = "is_liked")
    })
    ReplyDTO getReplyWithLikeStatus(@Param("replyId") Long replyId, @Param("userId") Integer userId);

    /**
     * 删除回复
     */
    @Update("UPDATE post_comment SET status = 0 WHERE comment_id = #{replyId} AND user_id = #{userId}")
    int deleteReply(@Param("replyId") Long replyId, @Param("userId") Integer userId);

    /**
     * 获取回复点赞状态
     */
    @Select("SELECT * FROM post_comment_like WHERE comment_id = #{replyId} AND user_id = #{userId}")
    ReplyLike getReplyLike(@Param("replyId") Long replyId, @Param("userId") Integer userId);

    /**
     * 插入回复点赞
     */
    @Insert("INSERT INTO post_comment_like (comment_id, user_id, created_at, status) VALUES (#{replyId}, #{userId}, #{createTime}, 1)")
    void insertReplyLike(ReplyLike replyLike);

    /**
     * 更新回复点赞状态
     */
    @Update("UPDATE post_comment_like SET status = #{status}, created_at = #{createTime} WHERE comment_id = #{replyId} AND user_id = #{userId}")
    void updateReplyLike(ReplyLike replyLike);

    /**
     * 更新回复点赞数
     */
    @Update("UPDATE post_comment SET like_count = like_count + #{increment} WHERE comment_id = #{replyId}")
    void updateReplyLikeCount(@Param("replyId") Long replyId, @Param("increment") Integer increment);

    /**
     * 批量逻辑删除某个评论下的所有回复
     */
    @Update("UPDATE post_comment SET status = 0 WHERE parent_id = #{commentId} AND status = 1")
    int deleteRepliesByCommentId(@Param("commentId") Long commentId);

    /**
     * 获取回复
     */
    @Select("SELECT * FROM post_comment WHERE comment_id = #{replyId}")
    Reply getReply(Long replyId);

    /**
     * 更新回复状态
     */
    @Update("UPDATE post_comment SET status = #{status} WHERE comment_id = #{replyId}")
    int updateReplyStatus(@Param("replyId") Long replyId, @Param("status") Integer status);

    /**
     * 获取评论的最近回复列表
     */
    @Select("SELECT r.comment_id, r.parent_id, r.root_id, r.user_id, r.content, r.like_count, r.create_time, " +
            "u.username, u.avatar FROM post_comment r " +
            "JOIN users u ON r.user_id = u.user_id " +
            "WHERE r.parent_id = #{commentId} AND r.status = 1 " +
            "ORDER BY r.create_time DESC " +
            "LIMIT 3")
    @Results({
        @Result(property = "replyId", column = "comment_id"),
        @Result(property = "parentId", column = "parent_id"),
        @Result(property = "rootId", column = "root_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "likeCount", column = "like_count"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "username", column = "username"),
        @Result(property = "avatar", column = "avatar")
    })
    List<ReplyDTO> getRecentRepliesByCommentId(Long commentId);
}