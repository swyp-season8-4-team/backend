package org.swyp.dessertbee.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.auth.entity.AuthEntity;
import org.swyp.dessertbee.auth.repository.AuthRepository;
import org.swyp.dessertbee.preference.entity.PreferenceEntity;
import org.swyp.dessertbee.preference.repository.PreferenceRepository;
import org.swyp.dessertbee.preference.service.PreferenceService;
import org.swyp.dessertbee.role.entity.RoleEntity;
import org.swyp.dessertbee.role.entity.RoleType;
import org.swyp.dessertbee.role.repository.RoleRepository;
import org.swyp.dessertbee.user.entity.MbtiEntity;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.MbtiRepository;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MbtiRepository mbtiRepository;
    private final RoleRepository roleRepository;
    private final PreferenceRepository preferenceRepository;
    private final PreferenceService preferenceService;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;

    // 사용된 닉네임을 추적하기 위한 세트
    private final Set<String> usedNicknames = new HashSet<>();

    private static final String[] FIRST_NAMES = {
            "민준", "서준", "도윤", "예준", "시우", "하준", "주원", "지호", "지후", "준서",
            "서연", "서윤", "지우", "하윤", "민서", "하은", "윤서", "지유", "채원", "수아",
            "지민", "지훈", "민재", "건우", "현우", "우진", "준영", "현준", "민수", "재현"
    };

    private static final String[] LAST_NAMES = {
            "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임",
            "한", "오", "서", "신", "권", "황", "안", "송", "류", "전"
    };

    private static final String[] DESSERT_PREFIXES = {
            "달콤한", "달달한", "맛있는", "행복한", "즐거운", "신나는", "향긋한", "부드러운", "촉촉한", "바삭한",
            "폭신한", "꿀맛", "사랑스러운", "아기자기한", "매력적인", "환상적인", "상큼한", "고소한", "진한", "깊은"
    };

    private static final String[] DESSERT_SUFFIXES = {
            "마카롱", "쿠키", "케이크", "머핀", "초코", "크림", "베이커리", "디저트", "꿀벌", "파티쉐",
            "제빵왕", "디저트킹", "빵순이", "디저트퀸", "스위트", "쿠킹맘", "쿠킹대디", "베이킹", "맛집탐험가", "디저트덕후"
    };

    private static final String[] KOREAN_ADDRESSES = {
            "서울특별시 강남구", "서울특별시 서초구", "서울특별시 송파구", "서울특별시 마포구", "서울특별시 용산구",
            "서울특별시 중구", "서울특별시 종로구", "서울특별시 동작구", "서울특별시 강서구", "서울특별시 노원구",
            "경기도 성남시 분당구", "경기도 수원시 영통구", "경기도 고양시 일산동구", "경기도 용인시 수지구", "경기도 부천시"
    };

    private static final String[] STREETS = {
            "테헤란로", "강남대로", "서초대로", "영동대로", "압구정로", "도산대로", "삼성로", "종로", "을지로", "충무로",
            "홍대입구", "신촌로", "명동길", "가로수길", "청담로", "신사동", "잠실로", "양재천로", "한강로", "청계천로"
    };

    @Override
    @Transactional
    public void run(String... args) {
        // 기존 데이터 확인
        long userCount = userRepository.count();
        if (userCount >= 30) {
            System.out.println("Users already seeded. Skipping user seeding.");
            return;
        }

        // MBTI 목록 로드
        List<MbtiEntity> mbtiList = mbtiRepository.findAll();
        if (mbtiList.isEmpty()) {
            System.out.println("No MBTI entities found. Please run MbtiSeeder first.");
            return;
        }

        // 역할 목록 로드
        RoleEntity userRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default user role not found"));
        RoleEntity ownerRole = roleRepository.findByName(RoleType.ROLE_OWNER)
                .orElseThrow(() -> new RuntimeException("Owner role not found"));

        // 선호도 목록 로드
        List<PreferenceEntity> preferences = preferenceRepository.findAll();
        if (preferences.isEmpty()) {
            System.out.println("No preference entities found. Please run PreferenceSeeder first.");
            return;
        }

        // 기존에 존재하는 닉네임 추적
        userRepository.findAll().forEach(user -> usedNicknames.add(user.getNickname()));

        // 일반 사용자 생성 (20명)
        createUsers(userRole, mbtiList, preferences, 20);

        // 사업자 계정 생성 (10명)
        createOwners(ownerRole, mbtiList, preferences, 10);

        System.out.println("User seeding completed successfully.");
    }

    private void createUsers(RoleEntity userRole, List<MbtiEntity> mbtiList,
                             List<PreferenceEntity> preferences, int count) {

        for (int i = 0; i < count; i++) {
            // 사용자 이름 생성
            String firstName = getRandomItem(FIRST_NAMES);
            String lastName = getRandomItem(LAST_NAMES);
            String fullName = lastName + firstName;

            // 이메일 생성
            String email = "user" + (i + 1) + "@dessertbee.com";

            // 디저트 관련 닉네임 생성 (중복 방지)
            String nickname = generateUniqueDesertNickname();

            // 랜덤 전화번호 생성
            String phoneNumber = generateKoreanPhoneNumber();

            // 성별 랜덤 선택
            UserEntity.Gender gender = ThreadLocalRandom.current().nextBoolean() ?
                    UserEntity.Gender.MALE : UserEntity.Gender.FEMALE;

            // 랜덤 주소 생성
            String address = getRandomItem(KOREAN_ADDRESSES) + " " +
                    getRandomItem(STREETS) + " " +
                    ThreadLocalRandom.current().nextInt(1, 200) + "번길 " +
                    ThreadLocalRandom.current().nextInt(1, 50);

            // 비밀번호 생성 및 암호화
            String password = passwordEncoder.encode("Test1234!");

            // 생성 시간 설정 (최근 3개월 이내)
            LocalDateTime createdAt = LocalDateTime.now().minusDays(
                    ThreadLocalRandom.current().nextLong(1, 90)
            );

            // 사용자 엔티티 생성 - HashSet으로 userPreferences 초기화
            UserEntity user = UserEntity.builder()
                    .email(email)
                    .password(password)
                    .name(fullName)
                    .nickname(nickname)
                    .phoneNumber(phoneNumber)
                    .address(address)
                    .gender(gender)
                    .mbti(getRandomItem(mbtiList))
                    .userPreferences(new HashSet<>())  // 빈 HashSet으로 초기화
                    .createdAt(createdAt)
                    .updatedAt(createdAt)
                    .build();

            // 역할 할당
            user.addRole(userRole);

            // 사용자 저장
            UserEntity savedUser = userRepository.save(user);

            // 인증 정보 추가
            AuthEntity auth = AuthEntity.builder()
                    .user(savedUser)
                    .provider("local")
                    .providerId(email)
                    .active(true)
                    .createdAt(createdAt)
                    .updatedAt(createdAt)
                    .build();
            authRepository.save(auth);

            // 선호도 할당 (랜덤 1-5개)
            List<Long> preferenceIds = getRandomPreferenceIds(preferences, 1, 5);
            preferenceService.updateUserPreferences(savedUser, preferenceIds);

            System.out.println("Created user: " + savedUser.getEmail() + " (" + savedUser.getNickname() + ")");
        }
    }

    private void createOwners(RoleEntity ownerRole, List<MbtiEntity> mbtiList,
                              List<PreferenceEntity> preferences, int count) {

        for (int i = 0; i < count; i++) {
            // 사업자 이름 생성
            String firstName = getRandomItem(FIRST_NAMES);
            String lastName = getRandomItem(LAST_NAMES);
            String fullName = lastName + firstName;

            // 이메일 생성
            String email = "owner" + (i + 1) + "@dessertbee.com";

            // 디저트 관련 닉네임 생성 (사업자 느낌, 중복 방지)
            String nickname = "디저트사장" + (i + 1);
            // 닉네임 중복 방지 확인
            if (usedNicknames.contains(nickname)) {
                nickname = "디저트사장" + (i + 1) + UUID.randomUUID().toString().substring(0, 4);
            }
            usedNicknames.add(nickname);

            // 랜덤 전화번호 생성
            String phoneNumber = generateKoreanPhoneNumber();

            // 성별 랜덤 선택
            UserEntity.Gender gender = ThreadLocalRandom.current().nextBoolean() ?
                    UserEntity.Gender.MALE : UserEntity.Gender.FEMALE;

            // 랜덤 주소 생성
            String address = getRandomItem(KOREAN_ADDRESSES) + " " +
                    getRandomItem(STREETS) + " " +
                    ThreadLocalRandom.current().nextInt(1, 200) + "번길 " +
                    ThreadLocalRandom.current().nextInt(1, 50);

            // 비밀번호 생성 및 암호화
            String password = passwordEncoder.encode("Test1234!");

            // 생성 시간 설정 (최근 6개월 이내)
            LocalDateTime createdAt = LocalDateTime.now().minusDays(
                    ThreadLocalRandom.current().nextLong(1, 180)
            );

            // 사용자 엔티티 생성 - HashSet으로 userPreferences 초기화
            UserEntity owner = UserEntity.builder()
                    .email(email)
                    .password(password)
                    .name(fullName)
                    .nickname(nickname)
                    .phoneNumber(phoneNumber)
                    .address(address)
                    .gender(gender)
                    .mbti(getRandomItem(mbtiList))
                    .userPreferences(new HashSet<>())  // 빈 HashSet으로 초기화
                    .createdAt(createdAt)
                    .updatedAt(createdAt)
                    .build();

            // 역할 할당
            owner.addRole(ownerRole);

            // 사용자 저장
            UserEntity savedOwner = userRepository.save(owner);

            // 인증 정보 추가
            AuthEntity auth = AuthEntity.builder()
                    .user(savedOwner)
                    .provider("local")
                    .providerId(email)
                    .active(true)
                    .createdAt(createdAt)
                    .updatedAt(createdAt)
                    .build();
            authRepository.save(auth);

            // 선호도 할당 (랜덤 2-4개)
            List<Long> preferenceIds = getRandomPreferenceIds(preferences, 2, 4);
            preferenceService.updateUserPreferences(savedOwner, preferenceIds);

            System.out.println("Created owner: " + savedOwner.getEmail() + " (" + savedOwner.getNickname() + ")");
        }
    }

    /**
     * 고유한 디저트 닉네임 생성 (중복 방지)
     */
    private String generateUniqueDesertNickname() {
        String nickname;
        int attempts = 0;
        do {
            String prefix = getRandomItem(DESSERT_PREFIXES);
            String suffix = getRandomItem(DESSERT_SUFFIXES);

            // 항상 고유한 닉네임을 위해 랜덤 숫자를 추가
            int number = ThreadLocalRandom.current().nextInt(100, 10000);
            nickname = prefix + suffix + number;

            attempts++;

            // 무한 루프 방지 (50번 시도 후에도 고유한 닉네임을 찾지 못하면 UUID 추가)
            if (attempts > 50) {
                nickname = prefix + suffix + UUID.randomUUID().toString().substring(0, 8);
                break;
            }
        } while (usedNicknames.contains(nickname));

        // 사용된 닉네임 목록에 추가
        usedNicknames.add(nickname);
        return nickname;
    }

    private String generateKoreanPhoneNumber() {
        int mid = ThreadLocalRandom.current().nextInt(1000, 9999);
        int end = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "010-" + mid + "-" + end;
    }

    private List<Long> getRandomPreferenceIds(List<PreferenceEntity> preferences, int min, int max) {
        int prefCount = ThreadLocalRandom.current().nextInt(min, max + 1);
        List<PreferenceEntity> shuffledPrefs = new ArrayList<>(preferences);
        Collections.shuffle(shuffledPrefs);

        return shuffledPrefs.stream()
                .limit(Math.min(prefCount, preferences.size()))
                .map(PreferenceEntity::getId)
                .collect(Collectors.toList());
    }

    private <T> T getRandomItem(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    private <T> T getRandomItem(T[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return array[ThreadLocalRandom.current().nextInt(array.length)];
    }
}