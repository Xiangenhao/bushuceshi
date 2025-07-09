package org.example.afd.mapper;

import org.apache.ibatis.annotations.*;
import org.example.afd.pojo.Comment;
import org.example.afd.pojo.CommentLike;
import org.example.afd.dto.CommentDTO;

import java.util.List;

@Mapper
public interface CommentMapper {
    /**
     * 插入新评论
     */
    @Insert("INSERT INTO post_comment (post_id, user_id, content, like_count, reply_count, status, create_time) " +
            "VALUES (#{postId}, #{userId}, #{content}, 0, 0, 1, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "commentId")
    void insertComment(Comment comment);

    /**
     * 获取动态的评论列表
     */
    @Select("SELECT c.*, u.username, u.avatar FROM post_comment c " +
            "JOIN users u ON c.user_id = u.user_id " +
            "WHERE c.post_id = #{postId} AND c.status = 1 " +
            "ORDER BY c.create_time DESC " +
            "LIMIT #{offset}, #{size}")
    @Results({
        @Result(property = "commentId", column = "comment_id"),
        @Result(property = "postId", column = "post_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "parentId", column = "parent_id"),
        @Result(property = "rootId", column = "root_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "likeCount", column = "like_count"),
        @Result(property = "replyCount", column = "reply_count"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "username", column = "username"),
        @Result(property = "avatar", column = "avatar")
    })
    List<CommentDTO> getCommentsByPostId(@Param("postId") Long postId, @Param("offset") Integer offset, @Param("size") Integer size);

    /**
     * 获取评论详情
     */
    @Select("SELECT c.*, u.username, u.avatar FROM post_comment c " +
            "JOIN users u ON c.user_id = u.user_id " +
            "WHERE c.comment_id = #{commentId} AND c.status = 1")
    @Results({
        @Result(property = "commentId", column = "comment_id"),
        @Result(property = "postId", column = "post_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "parentId", column = "parent_id"),
        @Result(property = "rootId", column = "root_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "likeCount", column = "like_count"),
        @Result(property = "replyCount", column = "reply_count"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "username", column = "username"),
        @Result(property = "avatar", column = "avatar"),
        @Result(property = "status", column = "status")
    })
    CommentDTO getCommentById(Long commentId);

    /**
     * 获取评论详情，包含点赞状态
     */
    @Select("SELECT c.*, u.username, u.avatar, " +
            "IF(cl.id IS NULL, 0, 1) AS is_liked " +
            "FROM post_comment c " +
            "JOIN users u ON c.user_id = u.user_id " +
            "LEFT JOIN post_comment_like cl ON c.comment_id = cl.comment_id AND cl.user_id = #{userId} AND cl.status = 1 " +
            "WHERE c.comment_id = #{commentId} AND c.status = 1")
    @Results({
        @Result(property = "commentId", column = "comment_id"),
        @Result(property = "postId", column = "post_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "parentId", column = "parent_id"),
        @Result(property = "rootId", column = "root_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "likeCount", column = "like_count"),
        @Result(property = "replyCount", column = "reply_count"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "username", column = "username"),
        @Result(property = "avatar", column = "avatar"),
        @Result(property = "isLiked", column = "is_liked"),
        @Result(property = "status", column = "status")
    })
    CommentDTO getCommentWithLikeStatus(@Param("commentId") Long commentId, @Param("userId") Integer userId);

    /**
     * 获取动态的评论列表，包含点赞状态
     */
    @Select("SELECT c.*, u.username, u.avatar, " +
            "IF(cl.id IS NULL, 0, 1) AS is_liked " +
            "FROM post_comment c " +
            "JOIN users u ON c.user_id = u.user_id " +
            "LEFT JOIN post_comment_like cl ON c.comment_id = cl.comment_id AND cl.user_id = #{userId} AND cl.status = 1 " +
            "WHERE c.post_id = #{postId} AND c.parent_id IS NULL AND c.status = 1 " +
            "ORDER BY c.create_time DESC " +
            "LIMIT #{offset}, #{size}")
    @Results({
        @Result(property = "commentId", column = "comment_id"),
        @Result(property = "postId", column = "post_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "parentId", column = "parent_id"),
        @Result(property = "rootId", column = "root_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "likeCount", column = "like_count"),
        @Result(property = "replyCount", column = "reply_count"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "username", column = "username"),
        @Result(property = "avatar", column = "avatar"),
        @Result(property = "isLiked", column = "is_liked"),
        @Result(property = "status", column = "status")
    })
    List<CommentDTO> getCommentsWithLikeStatus(@Param("postId") Long postId, @Param("userId") Integer userId, @Param("offset") Integer offset, @Param("size") Integer size);

    /**
     * 删除评论
     */
    @Update("UPDATE post_comment SET status = 0 WHERE comment_id = #{commentId} AND user_id = #{userId}")
    int deleteComment(@Param("commentId") Long commentId, @Param("userId") Integer userId);

    /**
     * 获取评论点赞状态
     */
    @Select("SELECT * FROM post_comment_like WHERE comment_id = #{commentId} AND user_id = #{userId}")
    CommentLike getCommentLike(@Param("commentId") Long commentId, @Param("userId") Integer userId);

    /**
     * 插入评论点赞
     */
    @Insert("INSERT INTO post_comment_like (comment_id, user_id, created_at, status) VALUES (#{commentId}, #{userId}, #{createTime}, 1)")
    void insertCommentLike(CommentLike commentLike);

    /**
     * 更新评论点赞状态
     */
    @Update("UPDATE post_comment_like SET status = #{status}, created_at = #{createTime} WHERE comment_id = #{commentId} AND user_id = #{userId}")
    void updateCommentLike(CommentLike commentLike);

    /**
     * 更新评论点赞数
     */
    @Update("UPDATE post_comment SET like_count = like_count + #{increment} WHERE comment_id = #{commentId}")
    void updateCommentLikeCount(@Param("commentId") Long commentId, @Param("increment") Integer increment);

    /**
     * 更新评论回复数
     */
    @Update("UPDATE post_comment SET reply_count = reply_count + #{increment} WHERE comment_id = #{commentId}")
    void updateCommentReplyCount(@Param("commentId") Long commentId, @Param("increment") Integer increment);

    /**
     * 更新动态评论数
     */
    @Update("UPDATE post_content SET comment_count = comment_count + #{increment} WHERE post_id = #{postId}")
    void updatePostCommentCount(@Param("postId") Long postId, @Param("increment") Integer increment);

    /**
     * 获取评论
     */
    @Select("SELECT c.* FROM post_comment c WHERE c.comment_id = #{commentId}")
    @Results({
        @Result(property = "commentId", column = "comment_id"),
        @Result(property = "postId", column = "post_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "parentId", column = "parent_id"),
        @Result(property = "rootId", column = "root_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "likeCount", column = "like_count"),
        @Result(property = "replyCount", column = "reply_count"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "status", column = "status")
    })
    CommentDTO getComment(Long commentId);

    /**
     * 更新评论状态
     */
    @Update("UPDATE post_comment SET status = #{status} WHERE comment_id = #{commentId}")
    int updateCommentStatus(@Param("commentId") Long commentId, @Param("status") Integer status);
} 