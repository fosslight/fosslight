package oss.fosslight.controller;

import org.junit.jupiter.api.Test;

class LicenseControllerTest {

    @Test
    void saveComment() {
//        saveComment 시나리오
//
//
//        Vresult가 valid가 되지 않앗을 때
//        -> Vresult errormap이 잇을 경우
//        -> status Ok
//
//        Vresult가 valid가 되었을 때
//        -> 정상 처리
//        -> status Ok랑 객체 내용 검사
//
//        Request 추적(Comments history)
//
//        1. registComment
//        <insert id="registComment" parameterType="oss.fosslight.domain.CommentsHistory"  useGeneratedKeys="true" keyProperty="commId">
//                INSERT INTO
//                COMMENTS_HISTORY
//        (
//                <if test="!@oss.fosslight.util.StringUtil@isEmpty(commId)">
//                COMM_ID ,
//			</if>
//        REFERENCE_ID
//                , REFERENCE_DIV
//                , STATUS
//                , STATUS_CODE
//                , CONTENTS
//                , CREATOR
//                , MODIFIER
//                , EXPANSION1
//		)
//        VALUES
//                (
//                <if test="!@oss.fosslight.util.StringUtil@isEmpty(commId)">
//			#{commId} ,
//			</if>
//			#{referenceId}
//			, #{referenceDiv}
//			, #{status}
//			, #{statusCode}
//			, #{contents}
//			, #{loginUserName}
//			, #{loginUserName}
//			, #{expansion1}
//		) ON DUPLICATE KEY UPDATE
//			<if test="!@oss.fosslight.util.StringUtil@isEmpty(commId)">
//                COMM_ID  = #{commId},
//			</if>
//        REFERENCE_ID = #{referenceId}
//			, REFERENCE_DIV = #{referenceDiv}
//			, CONTENTS = #{contents}
//			, MODIFIER = #{loginUserName}
//	</insert>
//
//          -> commId, referenceId, referenceDiv, status, statusCode, contents, loginUserName, expansion1 필요
//

//        2. deleteCommentUserTemp
//        	<update id="deleteCommentUserTemp" parameterType="oss.fosslight.domain.CommentsHistory">
//                UPDATE
//        COMMENTS_HISTORY
//                SET
//        USE_YN= 'N'
//        WHERE
//                USE_YN = 'Y'
//        AND REFERENCE_ID = #{referenceId}
//        AND REFERENCE_DIV = #{referenceDiv}
//        AND MODIFIER = #{loginUserName}
//	</update>
//      -> referenceId, referenceDiv, loginUserName 필요

//        3. getCommentInfo
//        select id="getCommentInfo" parameterType="String" resultType="oss.fosslight.domain.CommentsHistory">
//                SELECT /* 2018-07-19 choye 변경 */
//        T1.COMM_ID
//                , T1.REFERENCE_ID
//                , T1.REFERENCE_DIV
//                , T1.STATUS
//                , T1.CONTENTS
//                , T1.EXPANSION1
//                , T1.USE_YN
//                , T1.CREATOR
//                , (SELECT T2.USER_NAME FROM T2_USERS T2 WHERE T1.CREATOR = T2.USER_ID) AS CREATOR_NAME
//		, (SELECT S1.CD_DTL_NM FROM T2_CODE_DTL S1 LEFT OUTER JOIN T2_USERS S2 ON S1.CD_DTL_NO=S2.DIVISION WHERE S1.CD_NO='200' AND S2.USER_ID=T1.CREATOR) AS CREATOR_DIVISION_NAME
//		, T1.CREATED_DATE
//                , T1.MODIFIER
//                , (SELECT T3.USER_NAME FROM T2_USERS T3 WHERE T1.MODIFIER = T3.USER_ID) AS MODIFIER_NAME
//		, (SELECT S1.CD_DTL_NM FROM T2_CODE_DTL S1 LEFT OUTER JOIN T2_USERS S2 ON S1.CD_DTL_NO=S2.DIVISION WHERE S1.CD_NO='200' AND S2.USER_ID=T1.MODIFIER) AS MODIFIER_DIVISION_NAME
//		, T1.MODIFIED_DATE
//                , T1.STATUS_CODE
//        FROM
//        COMMENTS_HISTORY T1
//        WHERE T1.COMM_ID = #{commId}
//	</select>
//      -> commId 필요


//      mail 보내는 로직도 있어 mocking 할 필요 있음

    }

}
