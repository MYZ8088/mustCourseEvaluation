/**
 * 规则推荐引擎 - 基于结构化参数精准匹配课程
 * 
 * 特点：
 * - 不依赖AI，纯规则匹配
 * - 保证推荐准确性和可控性
 * - 多维度评分算法
 */
class RuleEngineService {
  /**
   * 主推荐方法
   * @param {Object} parameters - 结构化需求参数
   * @param {Array} courses - 所有可用课程列表
   * @returns {Array} 推荐的课程列表（已排序）
   */
  recommend(parameters, courses) {
    if (!courses || courses.length === 0) {
      return [];
    }

    // 检查是否有任何有效参数（包含学院和教师）
    const hasValidParams = parameters.courseType || 
                          (parameters.keywords && parameters.keywords.length > 0) ||
                          parameters.credits ||
                          parameters.difficulty ||
                          parameters.faculty ||
                          parameters.teacher;

    // 如果没有任何参数，返回热门推荐
    if (!hasValidParams) {
      return this.getDefaultRecommendations(courses);
    }

    // 第一步：硬过滤（必须满足的条件）
    let filtered = this._hardFilter(parameters, courses);

    if (filtered.length === 0) {
      // 如果硬过滤后没有结果，尝试放宽条件
      filtered = this._softFilter(parameters, courses);
    }

    // 如果放宽后还是没有结果，返回热门推荐
    if (filtered.length === 0) {
      return this.getDefaultRecommendations(courses);
    }

    // 第二步：计算匹配分数
    const scored = filtered.map(course => ({
      ...course,
      matchScore: this._calculateMatchScore(course, parameters)
    }));

    // 第三步：智能排序
    const sorted = this._intelligentSort(scored, parameters);

    // 第四步：限制返回数量（3-5门）
    return sorted.slice(0, 5);
  }

  /**
   * 硬过滤：必须满足的条件
   */
  _hardFilter(parameters, courses) {
    return courses.filter(course => {
      // 课程类型必须匹配
      if (parameters.courseType && course.type !== parameters.courseType) {
        return false;
      }

      // 学分必须在范围内（±0.5）
      if (parameters.credits) {
        const creditDiff = Math.abs(course.credits - parameters.credits);
        if (creditDiff > 0.5) {
          return false;
        }
      }

      // 学院必须匹配（精确匹配或包含匹配）
      if (parameters.faculty) {
        const courseFaculty = course.facultyName || course.faculty || '';
        if (!courseFaculty.includes(parameters.faculty) && 
            !parameters.faculty.includes(courseFaculty)) {
          return false;
        }
      }

      // 教师必须匹配（精确匹配）
      if (parameters.teacher) {
        const courseTeacher = course.teacherName || course.teacher || '';
        if (!courseTeacher.includes(parameters.teacher) && 
            !parameters.teacher.includes(courseTeacher)) {
          return false;
        }
      }

      return true;
    });
  }

  /**
   * 软过滤：放宽条件的过滤
   */
  _softFilter(parameters, courses) {
    return courses.filter(course => {
      // 学院匹配（放宽条件）
      if (parameters.faculty) {
        const courseFaculty = course.facultyName || course.faculty || '';
        if (courseFaculty.includes(parameters.faculty) || 
            parameters.faculty.includes(courseFaculty)) {
          return true;
        }
      }

      // 教师匹配（放宽条件）
      if (parameters.teacher) {
        const courseTeacher = course.teacherName || course.teacher || '';
        if (courseTeacher.includes(parameters.teacher) || 
            parameters.teacher.includes(courseTeacher)) {
          return true;
        }
      }

      // 课程类型匹配
      if (parameters.courseType && course.type === parameters.courseType) {
        return true;
      }

      // 关键词匹配（扩展搜索范围到学院和教师名称）
      if (parameters.keywords && parameters.keywords.length > 0) {
        const searchText = `${course.name} ${course.code} ${course.description || ''} ${course.facultyName || ''} ${course.teacherName || ''}`.toLowerCase();
        return parameters.keywords.some(keyword => 
          searchText.includes(keyword.toLowerCase())
        );
      }

      return false;
    });
  }

  /**
   * 计算课程匹配分数（0-100分）
   */
  _calculateMatchScore(course, parameters) {
    let score = 0;

    // 1. 学院/教师精确匹配加分（0-30分）
    score += this._calculateFacultyTeacherScore(course, parameters);

    // 2. 关键词匹配度（0-25分）
    score += this._calculateKeywordScore(course, parameters.keywords) * 0.625; // 调整权重

    // 3. 评分权重（0-25分）
    score += this._calculateRatingScore(course) * 0.833; // 调整权重

    // 4. 评价数量（0-10分）- 反映课程热度
    score += this._calculatePopularityScore(course) * 0.667; // 调整权重

    // 5. 难度匹配（0-10分）
    score += this._calculateDifficultyScore(course, parameters.difficulty) * 0.667; // 调整权重

    return score;
  }

  /**
   * 学院和教师匹配分数
   */
  _calculateFacultyTeacherScore(course, parameters) {
    let score = 0;

    // 学院精确匹配（0-20分）
    if (parameters.faculty) {
      const courseFaculty = course.facultyName || course.faculty || '';
      if (courseFaculty.includes(parameters.faculty) || 
          parameters.faculty.includes(courseFaculty)) {
        score += 20;
      }
    }

    // 教师精确匹配（0-10分）
    if (parameters.teacher) {
      const courseTeacher = course.teacherName || course.teacher || '';
      if (courseTeacher.includes(parameters.teacher) || 
          parameters.teacher.includes(courseTeacher)) {
        score += 10;
      }
    }

    return score;
  }

  /**
   * 关键词匹配分数
   */
  _calculateKeywordScore(course, keywords) {
    if (!keywords || keywords.length === 0) {
      return 20; // 没有关键词要求，给基础分
    }

    // 扩展搜索范围：包含课程名称、代码、描述、学院名称、教师名称
    const searchText = `${course.name} ${course.code} ${course.description || ''} ${course.facultyName || ''} ${course.teacherName || ''}`.toLowerCase();
    let matchCount = 0;

    keywords.forEach(keyword => {
      if (searchText.includes(keyword.toLowerCase())) {
        matchCount++;
      }
    });

    // 匹配比例 × 40分
    return (matchCount / keywords.length) * 40;
  }

  /**
   * 评分权重分数
   */
  _calculateRatingScore(course) {
    if (!course.averageRating) {
      return 10; // 没有评分的课程给基础分
    }

    // 评分越高，分数越高（最高30分）
    return (course.averageRating / 5.0) * 30;
  }

  /**
   * 热度分数（基于评价数量）
   */
  _calculatePopularityScore(course) {
    if (!course.reviewCount) {
      return 5; // 基础分
    }

    // 评价数量越多，热度越高（最高15分）
    // 使用对数函数避免过度偏向高评价数课程
    const normalizedCount = Math.min(course.reviewCount, 100);
    return (Math.log10(normalizedCount + 1) / 2) * 15;
  }

  /**
   * 难度匹配分数
   */
  _calculateDifficultyScore(course, difficulty) {
    if (!difficulty || !course.averageRating) {
      return 10; // 没有难度要求，给基础分
    }

    // 使用评分作为难度参考：
    // - 评分高 ≈ 相对简单/教学好
    // - 评分低 ≈ 可能较难
    const rating = course.averageRating;

    if (difficulty === 'easy') {
      // 希望简单：评分越高越好
      return rating >= 4.0 ? 15 : rating >= 3.5 ? 10 : 5;
    } else if (difficulty === 'hard') {
      // 希望有挑战：评分中等的可能更有挑战性
      return rating >= 3.0 && rating <= 4.0 ? 15 : 10;
    } else if (difficulty === 'medium') {
      // 希望中等：评分在3.5-4.5之间
      return rating >= 3.5 && rating <= 4.5 ? 15 : 10;
    }

    return 10;
  }

  /**
   * 智能排序
   */
  _intelligentSort(courses, parameters) {
    // 按匹配分数排序
    const sorted = courses.sort((a, b) => b.matchScore - a.matchScore);

    // 如果用户指定了学院或教师，不需要多样性（因为用户明确想要特定学院/教师的课程）
    if (parameters.faculty || parameters.teacher) {
      return sorted;
    }

    // 考虑多样性：避免同一教师或院系占据过多位置
    return this._diversify(sorted);
  }

  /**
   * 增加推荐多样性
   */
  _diversify(courses) {
    if (courses.length <= 3) {
      return courses;
    }

    const result = [];
    const usedTeachers = new Set();
    const usedFaculties = new Set();

    // 第一轮：选择不同教师和院系的课程
    for (const course of courses) {
      if (result.length >= 5) break;

      const teacherId = course.teacherId || course.teacherName;
      const facultyId = course.facultyId || course.facultyName;

      if (!usedTeachers.has(teacherId) && !usedFaculties.has(facultyId)) {
        result.push(course);
        if (teacherId) usedTeachers.add(teacherId);
        if (facultyId) usedFaculties.add(facultyId);
      }
    }

    // 第二轮：如果结果不足，填充剩余高分课程
    for (const course of courses) {
      if (result.length >= 5) break;
      if (!result.includes(course)) {
        result.push(course);
      }
    }

    return result;
  }

  /**
   * 解释推荐理由（基于分数组成）
   */
  explainRecommendation(course, parameters) {
    const reasons = [];

    // 学院匹配理由
    if (parameters.faculty && course.facultyName) {
      const courseFaculty = course.facultyName || '';
      if (courseFaculty.includes(parameters.faculty) || 
          parameters.faculty.includes(courseFaculty)) {
        reasons.push(`来自${course.facultyName}`);
      }
    }

    // 教师匹配理由
    if (parameters.teacher && course.teacherName) {
      const courseTeacher = course.teacherName || '';
      if (courseTeacher.includes(parameters.teacher) || 
          parameters.teacher.includes(courseTeacher)) {
        reasons.push(`由您指定的${course.teacherName}老师授课`);
      }
    } else if (course.teacherName) {
      reasons.push(`由${course.teacherName}老师授课`);
    }

    // 评分理由
    if (course.averageRating && course.averageRating >= 4.0) {
      reasons.push(`评分${course.averageRating.toFixed(1)}分，学生评价优秀`);
    }

    // 关键词匹配理由
    if (parameters.keywords && parameters.keywords.length > 0) {
      const searchText = `${course.name} ${course.description || ''} ${course.facultyName || ''}`.toLowerCase();
      const matchedKeywords = parameters.keywords.filter(kw => 
        searchText.includes(kw.toLowerCase())
      );
      if (matchedKeywords.length > 0) {
        reasons.push(`与您感兴趣的${matchedKeywords.join('、')}相关`);
      }
    }

    // 难度理由
    if (parameters.difficulty === 'easy' && course.averageRating >= 4.0) {
      reasons.push('课程难度适中，适合入门');
    }

    // 热度理由
    if (course.reviewCount && course.reviewCount > 10) {
      reasons.push(`已有${course.reviewCount}位同学评价`);
    }

    return reasons.length > 0 
      ? reasons.join('，') 
      : '符合您的基本要求';
  }

  /**
   * 检查参数是否完整
   * 修改策略：降低门槛，总是尝试推荐
   */
  hasEnoughParameters(parameters) {
    // 总是返回true，即使参数不完整也尝试推荐
    // 如果没有明确参数，将返回热门课程
    return true;
  }

  /**
   * 生成友好的补充建议（用于推荐后）
   * 修改策略：不再作为单独询问，而是作为推荐后的友好提示
   */
  suggestNextQuestion(parameters) {
    const suggestions = [];
    
    if (!parameters.faculty) {
      suggestions.push('感兴趣的学院或专业方向');
    }
    
    if (!parameters.courseType) {
      suggestions.push('课程类型（必修/选修）');
    }
    
    if (!parameters.keywords || parameters.keywords.length === 0) {
      suggestions.push('感兴趣的领域关键词');
    }
    
    if (!parameters.teacher) {
      suggestions.push('偏好的授课教师');
    }
    
    if (suggestions.length > 0) {
      return `💡 您还可以告诉我${suggestions.join('、')}等信息，我会为您进一步精准筛选！`;
    }
    
    return '如果您还有其他要求，请随时告诉我！';
  }

  /**
   * 获取默认推荐（热门/高评分课程）
   * 当用户没有提供明确参数时使用
   */
  getDefaultRecommendations(courses) {
    if (!courses || courses.length === 0) {
      return [];
    }

    // 计算综合分数：评分 + 热度
    const scored = courses.map(course => {
      let score = 0;
      
      // 评分权重（60%）
      if (course.averageRating) {
        score += (course.averageRating / 5.0) * 60;
      } else {
        score += 30; // 无评分给基础分
      }
      
      // 热度权重（40%）
      if (course.reviewCount) {
        const normalizedCount = Math.min(course.reviewCount, 100);
        score += (Math.log10(normalizedCount + 1) / 2) * 40;
      } else {
        score += 10; // 无评价数给基础分
      }
      
      return {
        ...course,
        matchScore: score
      };
    });

    // 按综合分数排序
    const sorted = scored.sort((a, b) => b.matchScore - a.matchScore);

    // 返回前5门热门课程
    return sorted.slice(0, 5);
  }
}

export default new RuleEngineService();


