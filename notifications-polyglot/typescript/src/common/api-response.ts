export class ApiResponse<T> {
    success: boolean;
    message: string;
    data?: T;

    static success<T>(data: T): ApiResponse<T> {
        return { success: true, message: 'Success', data };
    }

    static error<T>(message: string): ApiResponse<T> {
        return { success: false, message };
    }
}
